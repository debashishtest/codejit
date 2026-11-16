import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  AlertTriangle,
  ArrowLeft,
  Bot,
  Camera,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Clock,
  Eye,
  EyeOff,
  Flag,
  Lightbulb,
  LoaderCircle,
  Mic,
  MicOff,
  RotateCcw,
  Send,
  ShieldAlert,
  ShieldCheck,
  SkipForward,
  Sparkles,
  TrendingUp,
  Volume2,
  VolumeX,
  XCircle
} from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  api,
  type AiInterviewSession,
  type AiInterviewTurn,
  type AiMonitoringSummary,
  type AiMonitorSeverity
} from '../api'
import { notify } from '../notifications/notificationBus'
import {
  analyzeVideoFrameForEyeTracking,
  captureSnapshotBase64,
  playCheatingAlertTone,
  type LocalEyeObservation
} from '../utils/eyeMonitor'

type SpeechRecognitionAlternativeLike = {
  transcript: string
}

type SpeechRecognitionResultLike = {
  isFinal: boolean
  0: SpeechRecognitionAlternativeLike
}

type SpeechRecognitionResultEventLike = {
  resultIndex: number
  results: ArrayLike<SpeechRecognitionResultLike>
}

type SpeechRecognitionInstanceLike = {
  continuous: boolean
  interimResults: boolean
  lang: string
  onresult: ((event: SpeechRecognitionResultEventLike) => void) | null
  onerror: (() => void) | null
  onend: (() => void) | null
  start: () => void
  stop: () => void
}

function formatDuration(totalSeconds: number): string {
  const mins = Math.floor(Math.max(0, totalSeconds) / 60)
  const secs = Math.max(0, totalSeconds) % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

function blobToBase64(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onloadend = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      const commaIdx = result.indexOf(',')
      resolve(commaIdx >= 0 ? result.slice(commaIdx + 1) : result)
    }
    reader.onerror = () => reject(new Error('Failed to read audio recording'))
    reader.readAsDataURL(blob)
  })
}

export function AiInterviewRoomPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const sessionId = Number(id)

  const [session, setSession] = useState<AiInterviewSession | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [completing, setCompleting] = useState(false)
  const [requestingHint, setRequestingHint] = useState(false)

  // Voice-Only Response State (no text writing allowed)
  const [isSpeaking, setIsSpeaking] = useState(false)
  const [spokenTranscript, setSpokenTranscript] = useState('')
  const [interimTranscript, setInterimTranscript] = useState('')
  const [recordedAudioBase64, setRecordedAudioBase64] = useState<string | null>(null)
  const [recordedAudioMime, setRecordedAudioMime] = useState<string>('audio/webm')
  const [voiceLevel, setVoiceLevel] = useState(0)

  // Timers & Telemetry State
  const [elapsedSeconds, setElapsedSeconds] = useState(0)
  const [questionSeconds, setQuestionSeconds] = useState(0)
  const lastActivityRef = useRef<number>(0)
  const idleReportedForStreakRef = useRef<boolean>(false)
  const lastVisibilityHiddenAtRef = useRef<number>(0)

  // Always-On Eye Tracking & Cheating Alert State
  const [cameraActive, setCameraActive] = useState(false)
  const [cameraError, setCameraError] = useState('')
  const [eyeObservation, setEyeObservation] = useState<LocalEyeObservation>('EYES_ON_SCREEN')
  const [cheatingBanner, setCheatingBanner] = useState<string | null>(null)
  const [readAloud, setReadAloud] = useState(true)

  // Media / Speech / CV Refs
  const cameraVideoRef = useRef<HTMLVideoElement | null>(null)
  const cameraStreamRef = useRef<MediaStream | null>(null)
  const cvCanvasRef = useRef<HTMLCanvasElement | null>(null)
  const snapshotCanvasRef = useRef<HTMLCanvasElement | null>(null)
  const recognitionRef = useRef<SpeechRecognitionInstanceLike | null>(null)
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const recordedChunksRef = useRef<Blob[]>([])
  const audioContextRef = useRef<AudioContext | null>(null)
  const levelIntervalRef = useRef<number | null>(null)
  const lastSpokenTurnIdRef = useRef<number | null>(null)
  const suspiciousStreakRef = useRef<number>(0)
  const clearStreakRef = useRef<number>(0)
  const lastEyeAlertAtRef = useRef<number>(0)
  const lastPeriodicFrameAtRef = useRef<number>(0)
  const frameCheckInFlightRef = useRef<boolean>(false)
  const isSpeakingRef = useRef<boolean>(false)

  const speechSynthesisAvailable = typeof window !== 'undefined' && 'speechSynthesis' in window
  const speechRecognitionCtor = useMemo(() => {
    if (typeof window === 'undefined') return null
    const win = window as unknown as {
      SpeechRecognition?: new () => SpeechRecognitionInstanceLike
      webkitSpeechRecognition?: new () => SpeechRecognitionInstanceLike
    }
    return win.SpeechRecognition ?? win.webkitSpeechRecognition ?? null
  }, [])

  const activeTurn = useMemo(
    () => session?.turns.find((t) => t.status === 'ASKED') ?? null,
    [session]
  )

  const completedTurns = useMemo(
    () => session?.turns.filter((t) => t.status !== 'ASKED') ?? [],
    [session]
  )

  const stopSpeechCapture = useCallback(async (): Promise<{ base64: string | null; mime: string }> => {
    isSpeakingRef.current = false
    setIsSpeaking(false)
    setInterimTranscript('')
    setVoiceLevel(0)

    if (levelIntervalRef.current !== null) {
      window.clearInterval(levelIntervalRef.current)
      levelIntervalRef.current = null
    }
    if (audioContextRef.current) {
      void audioContextRef.current.close().catch(() => {})
      audioContextRef.current = null
    }
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop()
      } catch {
        // Ignore stop errors
      }
      recognitionRef.current = null
    }

    const recorder = mediaRecorderRef.current
    if (recorder && recorder.state !== 'inactive') {
      const mime = recorder.mimeType || 'audio/webm'
      const audioData = await new Promise<string | null>((resolve) => {
        recorder.onstop = async () => {
          try {
            if (recordedChunksRef.current.length === 0) {
              resolve(null)
              return
            }
            const blob = new Blob(recordedChunksRef.current, { type: mime })
            const b64 = await blobToBase64(blob)
            resolve(b64)
          } catch {
            resolve(null)
          }
        }
        try {
          recorder.stop()
        } catch {
          resolve(null)
        }
      })
      mediaRecorderRef.current = null
      if (audioData) {
        setRecordedAudioBase64(audioData)
        setRecordedAudioMime(mime)
      }
      return { base64: audioData, mime }
    }
    return { base64: recordedAudioBase64, mime: recordedAudioMime }
  }, [recordedAudioBase64, recordedAudioMime])

  const resetComposerForTurn = useCallback((turn: AiInterviewTurn | null) => {
    if (!turn) return
    setQuestionSeconds(0)
    setSpokenTranscript('')
    setInterimTranscript('')
    setRecordedAudioBase64(null)
    recordedChunksRef.current = []
    lastActivityRef.current = Date.now()
    idleReportedForStreakRef.current = false
  }, [])

  // 1. Load AI Interview Session
  useEffect(() => {
    if (!sessionId || Number.isNaN(sessionId)) {
      navigate('/ai-interviews')
      return
    }
    api.aiInterview(sessionId)
      .then((data) => {
        setSession(data)
        setElapsedSeconds(data.monitoring?.elapsedSeconds ?? 0)
        resetComposerForTurn(data.turns.find((t) => t.status === 'ASKED') ?? null)
      })
      .catch((err) => {
        notify({ kind: 'error', title: 'AI Interview Error', message: err instanceof Error ? err.message : 'Unable to load interview session' })
        navigate('/ai-interviews')
      })
  }, [sessionId, navigate, resetComposerForTurn])

  // 2. AI Voice Auto-Read for new questions
  const speakText = useCallback((text: string) => {
    if (!speechSynthesisAvailable || !text) return
    try {
      window.speechSynthesis.cancel()
      const utterance = new SpeechSynthesisUtterance(text)
      utterance.rate = 1.0
      window.speechSynthesis.speak(utterance)
    } catch {
      // Ignore speech synthesis errors
    }
  }, [speechSynthesisAvailable])

  useEffect(() => {
    if (!readAloud || !activeTurn || session?.status !== 'IN_PROGRESS') return
    if (lastSpokenTurnIdRef.current === activeTurn.id) return
    lastSpokenTurnIdRef.current = activeTurn.id
    speakText(activeTurn.questionText)
  }, [readAloud, activeTurn, session?.status, speakText])

  // 3. Telemetry & Cheating Alert Helper
  const currentSessionId = session?.id
  const currentSessionStatus = session?.status
  const currentTurnId = activeTurn?.id

  const triggerCheatingAlert = useCallback((message: string) => {
    setCheatingBanner(message)
    playCheatingAlertTone()
    notify({
      kind: 'error',
      title: 'Cheating Alert Detected',
      message,
    })
  }, [])

  const sendMonitorEvent = useCallback(
    async (eventType: string, severity: AiMonitorSeverity, detail: string, metadata?: string) => {
      if (!currentSessionId || currentSessionStatus !== 'IN_PROGRESS') return
      try {
        const updatedMonitoring: AiMonitoringSummary = await api.recordAiMonitorEvent(currentSessionId, {
          eventType,
          severity,
          detail,
          turnId: currentTurnId,
          metadata,
        })
        setSession((prev) => (prev ? { ...prev, monitoring: updatedMonitoring } : prev))
        const refreshed = await api.aiInterview(currentSessionId)
        setSession(refreshed)
      } catch {
        // Non-blocking telemetry error
      }
    },
    [currentSessionId, currentSessionStatus, currentTurnId]
  )

  // 4. Start Always-Active Camera + Mic Proctoring
  const startProctoringDevices = useCallback(async () => {
    if (cameraStreamRef.current) {
      return cameraStreamRef.current
    }
    try {
      if (!navigator.mediaDevices?.getUserMedia) {
        throw new Error('Camera and microphone APIs are unavailable in this browser.')
      }
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 320 }, height: { ideal: 240 } },
        audio: true,
      })
      cameraStreamRef.current = stream
      setCameraError('')
      setCameraActive(true)
      if (cameraVideoRef.current) {
        cameraVideoRef.current.srcObject = stream
        void cameraVideoRef.current.play().catch(() => {})
      }
      void sendMonitorEvent('CAMERA_ENABLED', 'INFO', 'Always-on eye tracking camera and microphone activated')
      return stream
    } catch {
      setCameraActive(false)
      const msg = 'Camera & microphone access was blocked. Proctored interview requires camera eye monitoring.'
      setCameraError(msg)
      triggerCheatingAlert(msg)
      void sendMonitorEvent('CAMERA_BLOCKED', 'ALERT', msg)
      return null
    }
  }, [sendMonitorEvent, triggerCheatingAlert])

  // Auto-attach stream if video element mounts after stream exists
  useEffect(() => {
    if (cameraActive && cameraStreamRef.current && cameraVideoRef.current && !cameraVideoRef.current.srcObject) {
      cameraVideoRef.current.srcObject = cameraStreamRef.current
      void cameraVideoRef.current.play().catch(() => {})
    }
  }, [cameraActive])

  // Auto-request camera & mic when IN_PROGRESS session begins
  useEffect(() => {
    if (!currentSessionId || currentSessionStatus !== 'IN_PROGRESS') return
    let active = true
    const init = async () => {
      await Promise.resolve()
      if (active) {
        await startProctoringDevices()
      }
    }
    void init()
    return () => {
      active = false
    }
  }, [currentSessionId, currentSessionStatus, startProctoringDevices])

  // 5. Continuous Eye & Face Cheating Monitoring Loop
  useEffect(() => {
    if (!currentSessionId || currentSessionStatus !== 'IN_PROGRESS' || !cameraActive) return
    if (!cvCanvasRef.current) cvCanvasRef.current = document.createElement('canvas')
    if (!snapshotCanvasRef.current) snapshotCanvasRef.current = document.createElement('canvas')

    const checkFrameWithServer = async (observationText: string, forceAlertMessage?: string) => {
      if (frameCheckInFlightRef.current || !cameraVideoRef.current || !snapshotCanvasRef.current) return
      const imageBase64 = captureSnapshotBase64(cameraVideoRef.current, snapshotCanvasRef.current)
      if (!imageBase64) return

      frameCheckInFlightRef.current = true
      try {
        const res = await api.monitorAiFrame(currentSessionId, {
          imageBase64,
          mimeType: 'image/jpeg',
          turnId: currentTurnId,
          localObservation: observationText,
        })
        setSession((prev) => (prev ? { ...prev, monitoring: res.monitoring } : prev))

        if (res.cheatingSuspected || !res.facePresent || res.multiplePeople || !res.lookingAtScreen) {
          const reason = res.alertReason ?? forceAlertMessage ?? `Suspicious eye gaze (${res.gazeDirection}) detected.`
          triggerCheatingAlert(reason)
          const refreshed = await api.aiInterview(currentSessionId)
          setSession(refreshed)
        }
      } catch {
        // Fallback if frame endpoint fails during local anomaly
        if (forceAlertMessage) {
          triggerCheatingAlert(forceAlertMessage)
          void sendMonitorEvent('EYE_GAZE_AWAY', 'ALERT', forceAlertMessage)
        }
      } finally {
        frameCheckInFlightRef.current = false
      }
    }

    const eyeInterval = window.setInterval(async () => {
      if (!cameraVideoRef.current || !cvCanvasRef.current) return
      const analysis = await analyzeVideoFrameForEyeTracking(cameraVideoRef.current, cvCanvasRef.current)
      setEyeObservation(analysis.observation)

      const now = Date.now()
      if (analysis.observation !== 'EYES_ON_SCREEN') {
        suspiciousStreakRef.current += 1
        clearStreakRef.current = 0

        if (suspiciousStreakRef.current >= 3 && now - lastEyeAlertAtRef.current >= 7000) {
          lastEyeAlertAtRef.current = now
          lastPeriodicFrameAtRef.current = now
          const reason = analysis.reason ?? 'Eyes looking away from the screen — cheating suspected.'
          await checkFrameWithServer(analysis.observation, reason)
        }
      } else {
        suspiciousStreakRef.current = 0
        clearStreakRef.current += 1
        if (clearStreakRef.current >= 4) {
          setCheatingBanner((prev) => (prev && !prev.includes('Tab') ? null : prev))
        }

        // Periodic Gemini Vision proctoring scan every 15s even when local CV looks normal
        if (now - lastPeriodicFrameAtRef.current >= 15000) {
          lastPeriodicFrameAtRef.current = now
          await checkFrameWithServer('EYES_ON_SCREEN')
        }
      }
    }, 800)

    return () => {
      window.clearInterval(eyeInterval)
    }
  }, [currentSessionId, currentSessionStatus, currentTurnId, cameraActive, sendMonitorEvent, triggerCheatingAlert])

  // 6. Session Timers, Tab Switch, Window Blur, and Inactivity Monitoring
  useEffect(() => {
    if (!currentSessionId || currentSessionStatus !== 'IN_PROGRESS') return
    lastActivityRef.current = Date.now()

    const tick = window.setInterval(() => {
      setElapsedSeconds((prev) => prev + 1)
      setQuestionSeconds((prev) => prev + 1)

      if (
        !isSpeakingRef.current &&
        !idleReportedForStreakRef.current &&
        Date.now() - lastActivityRef.current >= 90000
      ) {
        idleReportedForStreakRef.current = true
        void sendMonitorEvent(
          'IDLE_DETECTED',
          'WARNING',
          'Candidate remained silent/inactive for 90 seconds'
        )
      }
    }, 1000)

    const markActive = () => {
      lastActivityRef.current = Date.now()
      idleReportedForStreakRef.current = false
    }

    const onVisibilityChange = () => {
      if (document.hidden) {
        lastVisibilityHiddenAtRef.current = Date.now()
        triggerCheatingAlert('Tab switch detected! Stay on the interview tab during the session.')
        void sendMonitorEvent(
          'TAB_SWITCH',
          'ALERT',
          'Candidate switched away from the AI interview browser tab'
        )
      } else {
        markActive()
        void sendMonitorEvent(
          'FOCUS_REGAINED',
          'INFO',
          'Candidate returned focus to the AI interview room'
        )
      }
    }

    const onWindowBlur = () => {
      if (document.hidden || Date.now() - lastVisibilityHiddenAtRef.current < 1500) {
        return
      }
      triggerCheatingAlert('Window focus lost! Do not switch to external windows or notes.')
      void sendMonitorEvent(
        'WINDOW_BLUR',
        'WARNING',
        'Interview browser window lost focus'
      )
    }

    document.addEventListener('visibilitychange', onVisibilityChange)
    window.addEventListener('blur', onWindowBlur)
    window.addEventListener('mousedown', markActive)

    return () => {
      window.clearInterval(tick)
      document.removeEventListener('visibilitychange', onVisibilityChange)
      window.removeEventListener('blur', onWindowBlur)
      window.removeEventListener('mousedown', markActive)
    }
  }, [currentSessionId, currentSessionStatus, sendMonitorEvent, triggerCheatingAlert])

  // Cleanup all streams & speech on unmount
  useEffect(() => {
    return () => {
      cameraStreamRef.current?.getTracks().forEach((t) => t.stop())
      cameraStreamRef.current = null
      if (levelIntervalRef.current !== null) {
        window.clearInterval(levelIntervalRef.current)
      }
      if (audioContextRef.current) {
        void audioContextRef.current.close().catch(() => {})
      }
      try {
        recognitionRef.current?.stop()
      } catch {
        // Ignore
      }
      if (speechSynthesisAvailable) {
        try {
          window.speechSynthesis.cancel()
        } catch {
          // Ignore
        }
      }
    }
  }, [speechSynthesisAvailable])

  // 7. Start Speaking Answer (Voice-Only)
  const startSpeaking = async () => {
    lastActivityRef.current = Date.now()
    idleReportedForStreakRef.current = false

    let stream = cameraStreamRef.current
    if (!stream) {
      stream = await startProctoringDevices()
    }
    if (!stream) {
      // Fallback to audio-only if camera wasn't available
      try {
        stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        cameraStreamRef.current = stream
      } catch {
        notify({ kind: 'error', title: 'Microphone Required', message: 'Please allow microphone access to speak your answer.' })
        return
      }
    }

    const audioTracks = stream.getAudioTracks()
    if (audioTracks.length === 0) {
      notify({ kind: 'error', title: 'Microphone Missing', message: 'No audio input track detected.' })
      return
    }

    // Stop AI TTS before listening so mic doesn't pick up speaker echo
    if (speechSynthesisAvailable) {
      try {
        window.speechSynthesis.cancel()
      } catch {
        // Ignore
      }
    }

    recordedChunksRef.current = []
    setRecordedAudioBase64(null)
    isSpeakingRef.current = true
    setIsSpeaking(true)

    // 1) MediaRecorder for universal audio capture -> Gemini fallback transcription
    if (typeof MediaRecorder !== 'undefined') {
      try {
        const audioOnlyStream = new MediaStream(audioTracks)
        const preferredMime = MediaRecorder.isTypeSupported('audio/webm')
          ? 'audio/webm'
          : (MediaRecorder.isTypeSupported('audio/mp4') ? 'audio/mp4' : '')
        const recorder = preferredMime
          ? new MediaRecorder(audioOnlyStream, { mimeType: preferredMime })
          : new MediaRecorder(audioOnlyStream)
        recorder.ondataavailable = (e) => {
          if (e.data && e.data.size > 0) recordedChunksRef.current.push(e.data)
        }
        recorder.start(500)
        mediaRecorderRef.current = recorder
        setRecordedAudioMime(recorder.mimeType || preferredMime || 'audio/webm')
      } catch {
        // MediaRecorder optional if SpeechRecognition runs
      }
    }

    // 2) Live microphone level meter
    const AudioCtx = window.AudioContext ?? (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext
    if (AudioCtx) {
      try {
        const ctx = new AudioCtx()
        const source = ctx.createMediaStreamSource(new MediaStream(audioTracks))
        const analyser = ctx.createAnalyser()
        analyser.fftSize = 64
        source.connect(analyser)
        audioContextRef.current = ctx
        const buf = new Uint8Array(analyser.frequencyBinCount)
        levelIntervalRef.current = window.setInterval(() => {
          analyser.getByteFrequencyData(buf)
          let sum = 0
          for (let i = 0; i < buf.length; i++) sum += buf[i]
          setVoiceLevel(Math.min(100, Math.round((sum / buf.length) * 1.4)))
        }, 120)
      } catch {
        // Ignore visualizer failure
      }
    }

    // 3) Browser SpeechRecognition for live read-only captions
    if (speechRecognitionCtor) {
      try {
        const recognition = new speechRecognitionCtor()
        recognition.continuous = true
        recognition.interimResults = true
        recognition.lang = 'en-US'
        recognition.onresult = (event) => {
          let finalized = ''
          let interim = ''
          for (let i = event.resultIndex; i < event.results.length; i++) {
            const text = event.results[i][0]?.transcript ?? ''
            if (event.results[i].isFinal) finalized += `${text} `
            else interim += text
          }
          if (finalized.trim()) {
            setSpokenTranscript((prev) => (prev ? `${prev.trim()} ${finalized.trim()}` : finalized.trim()))
          }
          setInterimTranscript(interim.trim())
          lastActivityRef.current = Date.now()
        }
        recognition.onerror = () => {
          // Audio MediaRecorder still captures voice even if SpeechRecognition fails
        }
        recognition.onend = () => {
          setInterimTranscript('')
        }
        recognitionRef.current = recognition
        recognition.start()
      } catch {
        // Ignore
      }
    }

    void sendMonitorEvent('VOICE_USED', 'INFO', 'Candidate started speaking answer')
  }

  const handleToggleSpeak = async () => {
    if (isSpeaking) {
      await stopSpeechCapture()
    } else {
      await startSpeaking()
    }
  }

  const handleClearSpokenAnswer = async () => {
    if (isSpeaking) {
      await stopSpeechCapture()
    }
    setSpokenTranscript('')
    setInterimTranscript('')
    setRecordedAudioBase64(null)
    recordedChunksRef.current = []
  }

  const handleRequestHint = async () => {
    if (!session || !activeTurn) return
    setRequestingHint(true)
    try {
      const updated = await api.requestAiHint(session.id, activeTurn.id)
      setSession(updated)
    } catch (err) {
      notify({ kind: 'error', title: 'Hint Unavailable', message: err instanceof Error ? err.message : 'Unable to reveal hint' })
    } finally {
      setRequestingHint(false)
    }
  }

  const handleSubmitSpokenAnswer = async (skipped = false) => {
    if (!session || !activeTurn) return

    let audioB64 = recordedAudioBase64
    let audioMime = recordedAudioMime
    if (isSpeaking) {
      const stopped = await stopSpeechCapture()
      if (stopped.base64) {
        audioB64 = stopped.base64
        audioMime = stopped.mime
      }
    }

    const transcript = `${spokenTranscript} ${interimTranscript}`.trim()
    if (!skipped && !transcript && !audioB64) {
      notify({
        kind: 'error',
        title: 'Spoken Answer Required',
        message: 'Click "Start Speaking Answer" and answer using your microphone before submitting.',
      })
      return
    }

    setSubmitting(true)
    try {
      const updated = await api.submitAiAnswer(session.id, {
        turnId: activeTurn.id,
        answerText: transcript,
        audioBase64: !skipped && audioB64 ? audioB64 : undefined,
        audioMimeType: !skipped && audioB64 ? audioMime : undefined,
        responseTimeSeconds: questionSeconds,
        skipped,
      })
      setSession(updated)
      resetComposerForTurn(updated.turns.find((t) => t.status === 'ASKED') ?? null)

      const justEvaluated = updated.turns.find((t) => t.id === activeTurn.id)
      if (!skipped && justEvaluated?.evaluation) {
        notify({
          kind: justEvaluated.evaluation.score >= 65 ? 'success' : 'info',
          title: `Evaluated: ${justEvaluated.evaluation.score}/100 (${justEvaluated.evaluation.verdict.replace('_', ' ')})`,
          message: updated.status === 'COMPLETED'
            ? 'Final answer evaluated — complete AI interview report generated.'
            : 'Review AI feedback above and continue with the next question.',
        })
      }
    } catch (err) {
      notify({ kind: 'error', title: 'Submission Error', message: err instanceof Error ? err.message : 'Unable to evaluate spoken answer' })
    } finally {
      setSubmitting(false)
    }
  }

  const handleCompleteInterview = async () => {
    if (!session) return
    if (isSpeaking) {
      await stopSpeechCapture()
    }
    setCompleting(true)
    try {
      const updated = await api.completeAiInterview(session.id)
      setSession(updated)
      setCheatingBanner(null)
      notify({ kind: 'success', title: 'Interview Completed', message: 'Authentic AI evaluation & eye-monitoring report ready.' })
    } catch (err) {
      notify({ kind: 'error', title: 'Completion Error', message: err instanceof Error ? err.message : 'Unable to complete interview' })
    } finally {
      setCompleting(false)
    }
  }

  if (!session) {
    return (
      <div className="flex min-h-[calc(100vh-76px)] items-center justify-center bg-[#0b0d11]">
        <div className="flex items-center gap-3 border border-white/[.08] bg-[#11141a] px-6 py-4 text-sm text-[#858a98]">
          <LoaderCircle size={17} className="animate-spin text-[#f3c969]" />
          <span>Loading voice-proctored AI interview...</span>
        </div>
      </div>
    )
  }

  const monitoring = session.monitoring
  const mainQuestionsAsked = session.turns.filter((t) => !t.followUp).length
  const monitorBadgeStyle =
    monitoring.monitorStatus === 'OPTIMAL'
      ? 'border-[#89a96b]/40 bg-[#89a96b]/15 text-[#89a96b]'
      : monitoring.monitorStatus === 'ATTENTION_NEEDED'
        ? 'border-[#f3c969]/40 bg-[#f3c969]/15 text-[#f3c969]'
        : 'border-[#e56b5d]/40 bg-[#e56b5d]/15 text-[#e56b5d]'

  const eyeStatusOk = cameraActive && eyeObservation === 'EYES_ON_SCREEN'
  const liveTranscriptPreview = `${spokenTranscript} ${interimTranscript}`.trim()
  const hasSpokenInput = liveTranscriptPreview.length > 0 || Boolean(recordedAudioBase64)

  return (
    <div className="min-h-[calc(100vh-76px)] bg-[#0b0d11] p-3 sm:p-5">
      <div className="mx-auto max-w-[1560px] space-y-4">
        {/* Top Command Bar */}
        <header className="flex flex-wrap items-center justify-between gap-4 border border-white/[.08] bg-[#11141a] px-5 py-4">
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2.5">
              <Link to="/ai-interviews" className="mr-1 inline-flex items-center gap-1 text-xs text-[#858a98] hover:text-[#f3c969]">
                <ArrowLeft size={13} /> Hub
              </Link>
              <span className={`h-2.5 w-2.5 rounded-full ${session.status === 'IN_PROGRESS' ? 'bg-[#89a96b] shadow-[0_0_8px_#89a96b]' : 'bg-[#f3c969]'}`} />
              <span className="mono text-[10px] font-bold uppercase tracking-[.18em] text-[#f3c969]">
                {session.status === 'IN_PROGRESS' ? 'ORAL AI INTERVIEW · EYE MONITOR ACTIVE' : 'INTERVIEW COMPLETED'}
              </span>
              <span className="mono border border-white/[.12] px-2 py-0.5 text-[10px] text-[#c8cbd3]">
                {session.difficulty} · {session.mode.replace('_', ' ')}
              </span>
              {session.aiProviderUsed && (
                <span className="mono border border-[#f3c969]/30 bg-[#f3c969]/10 px-2 py-0.5 text-[10px] text-[#f3c969]">
                  {session.aiProviderUsed}
                </span>
              )}
            </div>
            <h1 className="text-xl font-extrabold tracking-tight text-white">{session.title}</h1>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <div className="flex items-center gap-2 border border-white/[.1] bg-[#0d0f14] px-3 py-2 text-xs text-[#d5d7df]">
              <Sparkles size={13} className="text-[#f3c969]" />
              <span className="mono">Q {Math.min(mainQuestionsAsked, session.questionCount)} / {session.questionCount}</span>
            </div>

            <div className="flex items-center gap-2 border border-white/[.1] bg-[#0d0f14] px-3 py-2 text-xs text-[#d5d7df]">
              <Clock size={13} className="text-[#f3c969]" />
              <span className="mono">{formatDuration(elapsedSeconds)} / {session.durationMinutes}m</span>
            </div>

            {speechSynthesisAvailable && session.status === 'IN_PROGRESS' && (
              <button
                type="button"
                onClick={() => setReadAloud(!readAloud)}
                className={`flex items-center gap-1.5 border px-3 py-2 text-xs font-bold transition ${readAloud ? 'border-[#f3c969] bg-[#f3c969]/15 text-[#f3c969]' : 'border-white/[.1] bg-[#0d0f14] text-[#858a98] hover:text-white'}`}
                title="Toggle AI speaking questions aloud"
              >
                {readAloud ? <Volume2 size={14} /> : <VolumeX size={14} />}
                <span className="hidden sm:inline">{readAloud ? 'AI Voice On' : 'AI Voice Off'}</span>
              </button>
            )}

            {session.status === 'IN_PROGRESS' && (
              <button
                type="button"
                disabled={completing}
                onClick={handleCompleteInterview}
                className="flex items-center gap-1.5 border border-[#e56b5d]/50 bg-[#e56b5d]/15 px-3.5 py-2 text-xs font-extrabold text-[#f08c7d] transition hover:bg-[#e56b5d] hover:text-white disabled:opacity-50"
              >
                <Flag size={13} /> {completing ? 'Generating Report...' : 'End & Report'}
              </button>
            )}
          </div>
        </header>

        {/* Live Cheating Alert Banner */}
        {session.status === 'IN_PROGRESS' && cheatingBanner && (
          <div
            role="alert"
            className="flex flex-wrap items-center justify-between gap-3 border-2 border-[#e56b5d] bg-[#2a1212] px-5 py-3.5 text-sm text-[#ffb4ab] shadow-[0_0_20px_rgba(229,107,93,0.25)]"
          >
            <div className="flex items-center gap-3">
              <ShieldAlert size={20} className="shrink-0 text-[#e56b5d] animate-pulse" />
              <div>
                <span className="mono text-xs font-extrabold uppercase tracking-wider text-[#ff7b6b]">
                  CHEATING / PROCTORING ALERT
                </span>
                <p className="text-xs font-semibold text-white">{cheatingBanner}</p>
              </div>
            </div>
            <button
              type="button"
              onClick={() => setCheatingBanner(null)}
              className="border border-white/20 px-2.5 py-1 text-[11px] font-bold text-white hover:bg-white/10"
            >
              Acknowledge
            </button>
          </div>
        )}

        {/* Main Split Layout */}
        <div className="grid gap-4 lg:grid-cols-[1fr_360px]">
          {/* Left Stage */}
          <div className="min-w-0 space-y-4">
            {session.status === 'COMPLETED' && session.finalReport && (
              <FinalReportSection session={session} />
            )}

            {/* Evaluated / Past Turns */}
            {completedTurns.length > 0 && (
              <section className="space-y-3">
                <div className="flex items-center justify-between px-1">
                  <span className="mono text-[10px] uppercase tracking-[.2em] text-[#858a98]">
                    Evaluated Questions &amp; AI Feedback ({completedTurns.length})
                  </span>
                </div>
                {completedTurns.map((turn, idx) => (
                  <EvaluatedTurnCard
                    key={turn.id}
                    turn={turn}
                    defaultExpanded={session.status === 'COMPLETED' || idx === completedTurns.length - 1}
                  />
                ))}
              </section>
            )}

            {/* Active Question & Speak-Only Response Workspace */}
            {session.status === 'IN_PROGRESS' && activeTurn && (
              <section className="border border-[#3b3523] bg-[#11141a]">
                <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/[.08] bg-[#161714] px-5 py-3.5">
                  <div className="flex flex-wrap items-center gap-2.5">
                    <div className="grid h-7 w-7 place-items-center bg-[#f3c969] text-[#17140d]">
                      <Bot size={15} />
                    </div>
                    <span className="mono text-xs font-bold text-[#f3c969]">
                      {activeTurn.followUp ? `AI FOLLOW-UP (TURN #${activeTurn.turnNumber})` : `AI QUESTION #${activeTurn.turnNumber}`}
                    </span>
                    <span className="mono border border-white/[.12] bg-[#0d0f14] px-2 py-0.5 text-[10px] text-[#c8cbd3]">
                      {activeTurn.category}
                    </span>
                    {activeTurn.topicTag && (
                      <span className="text-xs font-semibold text-[#858a98]">{activeTurn.topicTag}</span>
                    )}
                  </div>
                  <div className="flex items-center gap-2">
                    {speechSynthesisAvailable && (
                      <button
                        type="button"
                        onClick={() => speakText(activeTurn.questionText)}
                        className="flex items-center gap-1 border border-white/[.1] px-2.5 py-1 text-[11px] text-[#c8cbd3] hover:border-[#f3c969] hover:text-white"
                      >
                        <Volume2 size={12} /> Replay Question
                      </button>
                    )}
                    <span className="mono border border-white/[.1] bg-[#0d0f14] px-2.5 py-1 text-[11px] text-[#89a96b]">
                      {formatDuration(questionSeconds)} on Q
                    </span>
                  </div>
                </div>

                {/* Question Text */}
                <div className="border-b border-white/[.08] p-5 sm:p-6">
                  <p className="text-base font-semibold leading-7 text-white">{activeTurn.questionText}</p>

                  {/* Hints */}
                  <div className="mt-4 space-y-2">
                    {activeTurn.revealedHints.map((hint, idx) => (
                      <div key={idx} className="flex items-start gap-2.5 border border-[#f3c969]/30 bg-[#f3c969]/5 px-3.5 py-2.5 text-xs text-[#e8d5a3]">
                        <Lightbulb size={14} className="mt-0.5 shrink-0 text-[#f3c969]" />
                        <span><b>Hint {idx + 1}:</b> {hint}</span>
                      </div>
                    ))}
                    {activeTurn.revealedHints.length < activeTurn.totalHintsAvailable && (
                      <button
                        type="button"
                        disabled={requestingHint}
                        onClick={handleRequestHint}
                        className="inline-flex items-center gap-1.5 border border-white/[.12] bg-[#0d0f14] px-3 py-1.5 text-[11px] font-bold text-[#f3c969] hover:border-[#f3c969] disabled:opacity-50"
                      >
                        <Lightbulb size={13} />
                        {requestingHint
                          ? 'Loading hint...'
                          : `Reveal Hint (${activeTurn.totalHintsAvailable - activeTurn.revealedHints.length} available)`}
                      </button>
                    )}
                  </div>
                </div>

                {/* Speak-Only Answer Studio (No Writing / Typing Allowed) */}
                <div className="space-y-4 p-5 sm:p-6">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <span className="text-xs font-extrabold uppercase tracking-wider text-[#f3c969]">
                        Spoken Answer Only (No Typing)
                      </span>
                      <p className="mt-0.5 text-xs text-[#858a98]">
                        Look at the screen and explain your answer aloud into your microphone. Written input is disabled for authentic oral evaluation.
                      </p>
                    </div>

                    <div className="flex items-center gap-2">
                      {hasSpokenInput && (
                        <button
                          type="button"
                          disabled={submitting}
                          onClick={handleClearSpokenAnswer}
                          className="flex items-center gap-1.5 border border-white/[.12] bg-[#0d0f14] px-3 py-2 text-xs font-bold text-[#c8cbd3] hover:border-[#f3c969] hover:text-white"
                        >
                          <RotateCcw size={13} /> Re-record
                        </button>
                      )}
                      <button
                        type="button"
                        disabled={submitting}
                        onClick={handleToggleSpeak}
                        className={`flex items-center gap-2 px-4 py-2.5 text-xs font-extrabold transition ${
                          isSpeaking
                            ? 'border border-[#e56b5d] bg-[#e56b5d] text-white'
                            : 'bg-[#f3c969] text-[#17140d] hover:bg-[#e0b757]'
                        }`}
                      >
                        {isSpeaking ? <MicOff size={15} /> : <Mic size={15} />}
                        {isSpeaking ? 'Stop Speaking' : (hasSpokenInput ? 'Continue Speaking' : 'Start Speaking Answer')}
                      </button>
                    </div>
                  </div>

                  {/* Live Voice Waveform & Read-Only Transcript Panel */}
                  <div className={`border p-4 transition ${isSpeaking ? 'border-[#e56b5d]/70 bg-[#1a1114]' : 'border-white/[.1] bg-[#0d0f14]'}`}>
                    <div className="flex items-center justify-between border-b border-white/[.08] pb-2.5">
                      <div className="flex items-center gap-2">
                        <span className={`h-2 w-2 rounded-full ${isSpeaking ? 'bg-[#e56b5d] animate-ping' : hasSpokenInput ? 'bg-[#89a96b]' : 'bg-[#5e6370]'}`} />
                        <span className="mono text-[10px] font-bold uppercase tracking-wider text-[#c8cbd3]">
                          {isSpeaking
                            ? 'Listening to Microphone...'
                            : hasSpokenInput
                              ? 'Spoken Answer Captured'
                              : 'Microphone Ready — Press "Start Speaking Answer"'}
                        </span>
                      </div>

                      {/* Audio Volume Bar */}
                      <div className="flex items-center gap-2">
                        <span className="mono text-[9px] text-[#7f8492]">MIC LEVEL</span>
                        <div className="h-2 w-20 overflow-hidden bg-[#1a1d26]">
                          <div
                            className={`h-full transition-all duration-100 ${isSpeaking ? 'bg-[#f3c969]' : 'bg-[#414550]'}`}
                            style={{ width: `${isSpeaking ? Math.max(8, voiceLevel) : 0}%` }}
                          />
                        </div>
                      </div>
                    </div>

                    <div className="mt-3 min-h-[120px] select-none text-sm leading-6">
                      {liveTranscriptPreview ? (
                        <p className="text-white">
                          {spokenTranscript}
                          {interimTranscript && <span className="text-[#858a98]"> {interimTranscript}</span>}
                        </p>
                      ) : recordedAudioBase64 ? (
                        <p className="text-xs text-[#89a96b]">
                          Voice recording captured ({Math.round((recordedAudioBase64.length * 3) / 4096)} KB). Gemini AI will transcribe and grade your spoken response strictly when you submit.
                        </p>
                      ) : (
                        <p className="text-xs italic text-[#686d7a]">
                          {isSpeaking
                            ? 'Speak clearly — your voice is being recorded and transcribed live...'
                            : 'No spoken answer recorded yet. Press "Start Speaking Answer" and answer verbally.'}
                        </p>
                      )}
                    </div>
                  </div>

                  {/* Footer Action Bar */}
                  <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
                    <button
                      type="button"
                      disabled={submitting}
                      onClick={() => handleSubmitSpokenAnswer(true)}
                      className="flex items-center gap-1.5 border border-white/[.14] px-4 py-2.5 text-xs font-bold text-[#858a98] transition hover:border-white/30 hover:text-white disabled:opacity-50"
                    >
                      <SkipForward size={14} /> Skip Question (0 pts)
                    </button>

                    <button
                      type="button"
                      disabled={submitting}
                      onClick={() => handleSubmitSpokenAnswer(false)}
                      className="flex items-center gap-2 bg-[#f3c969] px-5 py-3 text-xs font-extrabold text-[#17140d] transition hover:bg-[#e0b757] disabled:opacity-50"
                    >
                      {submitting ? (
                        <>
                          <LoaderCircle size={15} className="animate-spin" /> AI Strictly Evaluating Voice...
                        </>
                      ) : (
                        <>
                          <Send size={14} /> Submit Spoken Answer
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </section>
            )}
          </div>

          {/* Right Sidebar: Always-On AI Eye & Cheating Monitor */}
          <aside className="space-y-4">
            <section className="space-y-4 border border-white/[.08] bg-[#11141a] p-4">
              <div className="flex items-center justify-between border-b border-white/[.08] pb-3">
                <div className="flex items-center gap-2">
                  <Eye size={15} className="text-[#f3c969]" />
                  <span className="text-xs font-extrabold uppercase tracking-wider text-white">AI Eye &amp; Proctor Monitor</span>
                </div>
                <span className={`mono border px-2 py-0.5 text-[10px] font-bold ${monitorBadgeStyle}`}>
                  {monitoring.monitorStatus.replace('_', ' ')}
                </span>
              </div>

              {/* Always-On Eye Tracking Webcam Box */}
              {session.status === 'IN_PROGRESS' && (
                <div className={`border p-2.5 space-y-2 ${!cameraActive || !eyeStatusOk ? 'border-[#e56b5d] bg-[#1f1214]' : 'border-[#89a96b]/40 bg-[#0d0f14]'}`}>
                  <div className="flex items-center justify-between gap-2">
                    <span className={`mono flex items-center gap-1.5 text-[10px] font-bold ${eyeStatusOk ? 'text-[#89a96b]' : 'text-[#ff7b6b]'}`}>
                      {eyeStatusOk ? <Eye size={12} /> : <EyeOff size={12} />}
                      {cameraActive ? eyeObservation.replace(/_/g, ' ') : 'CAMERA REQUIRED'}
                    </span>
                    {!cameraActive && (
                      <button
                        type="button"
                        onClick={() => void startProctoringDevices()}
                        className="flex items-center gap-1 bg-[#f3c969] px-2.5 py-1 text-[10px] font-extrabold text-[#17140d]"
                      >
                        <Camera size={11} /> Enable Camera
                      </button>
                    )}
                  </div>

                  <div className="relative aspect-video overflow-hidden bg-[#15181f]">
                    <video
                      ref={cameraVideoRef}
                      autoPlay
                      playsInline
                      muted
                      className={`h-full w-full object-cover ${cameraActive ? '' : 'hidden'}`}
                    />
                    {!cameraActive && (
                      <div className="flex h-full w-full flex-col items-center justify-center gap-2 px-3 text-center text-[#f08c7d]">
                        <AlertTriangle size={20} />
                        <span className="text-[11px] font-semibold">
                          {cameraError || 'Waiting for camera & microphone permission for continuous eye tracking...'}
                        </span>
                      </div>
                    )}
                    {cameraActive && (
                      <span className={`mono absolute bottom-2 left-2 px-2 py-0.5 text-[9px] font-bold ${eyeStatusOk ? 'bg-black/70 text-[#89a96b]' : 'bg-[#e56b5d] text-white'}`}>
                        {eyeStatusOk ? 'EYES ON SCREEN · OK' : 'CHEATING ALERT'}
                      </span>
                    )}
                  </div>
                </div>
              )}

              {/* Focus & Integrity Meter */}
              <div>
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-[#858a98]">Focus &amp; Integrity Score</span>
                  <span className="mono font-bold text-white">{monitoring.focusIntegrityScore}/100</span>
                </div>
                <div className="mt-2 h-2 w-full overflow-hidden border border-white/[.06] bg-[#0d0f14]">
                  <div
                    className={`h-full transition-all duration-300 ${monitoring.focusIntegrityScore >= 85 ? 'bg-[#89a96b]' : monitoring.focusIntegrityScore >= 65 ? 'bg-[#f3c969]' : 'bg-[#e56b5d]'}`}
                    style={{ width: `${monitoring.focusIntegrityScore}%` }}
                  />
                </div>
              </div>

              {/* Live Telemetry Grid */}
              <div className="grid grid-cols-2 gap-2 text-xs">
                <div className="border border-white/[.06] bg-[#0d0f14] p-2.5">
                  <span className="mono block text-[9px] text-[#7f8492]">CHEATING ALERTS</span>
                  <span className={`mono text-sm font-bold ${(monitoring.cheatingAlertCount ?? 0) > 0 ? 'text-[#e56b5d]' : 'text-[#89a96b]'}`}>
                    {monitoring.cheatingAlertCount ?? 0}
                  </span>
                </div>
                <div className="border border-white/[.06] bg-[#0d0f14] p-2.5">
                  <span className="mono block text-[9px] text-[#7f8492]">EYE OFF-SCREEN</span>
                  <span className={`mono text-sm font-bold ${(monitoring.eyeOffScreenCount ?? 0) > 0 ? 'text-[#f3c969]' : 'text-[#89a96b]'}`}>
                    {monitoring.eyeOffScreenCount ?? 0}
                  </span>
                </div>
                <div className="border border-white/[.06] bg-[#0d0f14] p-2.5">
                  <span className="mono block text-[9px] text-[#7f8492]">RUNNING AVG</span>
                  <span className="mono text-sm font-bold text-[#f3c969]">
                    {monitoring.questionsAnswered > 0 ? `${monitoring.runningAverageScore}%` : '—'}
                  </span>
                </div>
                <div className="border border-white/[.06] bg-[#0d0f14] p-2.5">
                  <span className="mono block text-[9px] text-[#7f8492]">TAB / FOCUS LOSS</span>
                  <span className={`mono text-sm font-bold ${monitoring.tabSwitchCount + monitoring.windowBlurCount > 0 ? 'text-[#f3c969]' : 'text-[#89a96b]'}`}>
                    {monitoring.tabSwitchCount} / {monitoring.windowBlurCount}
                  </span>
                </div>
              </div>

              {/* AI Monitor Live Observation */}
              <div className="space-y-2 border border-[#f3c969]/25 bg-[#f3c969]/5 p-3">
                <div className="flex items-center gap-1.5 text-[11px] font-bold text-[#f3c969]">
                  <ShieldCheck size={13} /> Live AI Proctor Insight
                </div>
                <p className="text-xs leading-5 text-[#d5d7df]">{monitoring.liveObservation}</p>
                {monitoring.alerts.length > 0 && (
                  <ul className="space-y-1 border-t border-white/[.08] pt-1">
                    {monitoring.alerts.map((alert, i) => (
                      <li key={i} className="flex items-start gap-1.5 text-[11px] text-[#f08c7d]">
                        <AlertTriangle size={12} className="mt-0.5 shrink-0" />
                        <span>{alert}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </section>

            {/* Live Audit Log */}
            <section className="flex flex-col border border-white/[.08] bg-[#11141a]">
              <div className="flex items-center justify-between border-b border-white/[.08] px-4 py-3">
                <span className="text-xs font-extrabold text-white">Monitoring Audit Log</span>
                <span className="mono text-[10px] text-[#858a98]">{session.monitorEvents.length} events</span>
              </div>
              <div className="max-h-[340px] divide-y divide-white/[.05] overflow-y-auto">
                {session.monitorEvents.map((ev) => (
                  <div key={ev.id} className="space-y-1 px-4 py-2.5">
                    <div className="flex items-center justify-between gap-2">
                      <span className={`mono text-[10px] font-bold ${ev.severity === 'ALERT' ? 'text-[#e56b5d]' : ev.severity === 'WARNING' ? 'text-[#f3c969]' : 'text-[#89a96b]'}`}>
                        {ev.eventType}
                      </span>
                      <span className="mono text-[9px] text-[#686d7a]">
                        {ev.focusScoreImpact < 0 ? `${ev.focusScoreImpact} focus` : ''}
                      </span>
                    </div>
                    <p className="text-[11px] leading-4 text-[#aeb2bd]">{ev.detail}</p>
                  </div>
                ))}
              </div>
            </section>
          </aside>
        </div>
      </div>
    </div>
  )
}

function EvaluatedTurnCard({ turn, defaultExpanded }: { turn: AiInterviewTurn; defaultExpanded: boolean }) {
  const [expanded, setExpanded] = useState(defaultExpanded)
  const evaluation = turn.evaluation
  const score = evaluation?.score ?? 0

  return (
    <div className="border border-white/[.08] bg-[#11141a]">
      <button
        type="button"
        onClick={() => setExpanded(!expanded)}
        className="flex w-full items-center justify-between gap-4 px-5 py-3.5 text-left hover:bg-white/[.02]"
      >
        <div className="flex min-w-0 flex-wrap items-center gap-2.5">
          {turn.status === 'SKIPPED' ? (
            <XCircle size={15} className="shrink-0 text-[#858a98]" />
          ) : score >= 65 ? (
            <CheckCircle2 size={15} className="shrink-0 text-[#89a96b]" />
          ) : (
            <ShieldAlert size={15} className="shrink-0 text-[#e56b5d]" />
          )}
          <span className="mono text-xs font-bold text-[#f3c969]">
            {turn.followUp ? `FOLLOW-UP #${turn.turnNumber}` : `Q${turn.turnNumber}`}
          </span>
          <span className="max-w-[540px] truncate text-xs font-semibold text-white">{turn.questionText}</span>
        </div>

        <div className="flex shrink-0 items-center gap-3">
          {evaluation && (
            <span className={`mono border px-2 py-0.5 text-[10px] font-bold ${score >= 65 ? 'border-[#89a96b]/40 bg-[#89a96b]/15 text-[#89a96b]' : score >= 40 ? 'border-[#f3c969]/40 bg-[#f3c969]/15 text-[#f3c969]' : 'border-[#e56b5d]/40 bg-[#e56b5d]/15 text-[#e56b5d]'}`}>
              {score}/100 · {evaluation.verdict.replace('_', ' ')}
            </span>
          )}
          {expanded ? <ChevronUp size={15} className="text-[#858a98]" /> : <ChevronDown size={15} className="text-[#858a98]" />}
        </div>
      </button>

      {expanded && (
        <div className="space-y-4 border-t border-white/[.08] bg-[#0d0f14] p-5 text-xs">
          <div>
            <span className="mono text-[10px] uppercase tracking-wider text-[#7f8492]">Question</span>
            <p className="mt-1 text-sm leading-6 text-white">{turn.questionText}</p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="border border-white/[.08] bg-[#11141a] p-3.5">
              <span className="mono text-[10px] uppercase tracking-wider text-[#7f8492]">Your Spoken Answer (Transcript)</span>
              <p className="mt-1.5 whitespace-pre-wrap leading-5 text-[#d5d7df]">
                {turn.userAnswer || (turn.status === 'SKIPPED' ? '(Skipped)' : '(No audible speech captured)')}
              </p>
            </div>

            {evaluation && (
              <div className="space-y-3 border border-[#f3c969]/25 bg-[#11141a] p-3.5">
                <div className="flex items-center justify-between">
                  <span className="mono text-[10px] uppercase tracking-wider text-[#f3c969]">Strict AI Evaluation</span>
                  <span className="mono text-xs font-bold text-white">{evaluation.score}/100</span>
                </div>
                <p className="leading-5 text-[#d5d7df]">{evaluation.feedback}</p>

                {turn.status === 'EVALUATED' && (
                  <div className="grid grid-cols-2 gap-2 pt-1">
                    <MiniMeter label="Accuracy" value={evaluation.technicalAccuracyScore} />
                    <MiniMeter label="Completeness" value={evaluation.completenessScore} />
                    <MiniMeter label="Clarity" value={evaluation.clarityScore} />
                    <MiniMeter label="Problem Solving" value={evaluation.problemSolvingScore} />
                  </div>
                )}
              </div>
            )}
          </div>

          {evaluation && (
            <div className="grid gap-3 sm:grid-cols-2">
              {evaluation.strengths.length > 0 && (
                <div className="border border-[#89a96b]/25 bg-[#89a96b]/5 p-3">
                  <span className="mono text-[10px] font-bold uppercase text-[#89a96b]">Strengths</span>
                  <ul className="mt-1.5 list-disc space-y-1 pl-4 text-[#c8cbd3]">
                    {evaluation.strengths.map((s, i) => <li key={i}>{s}</li>)}
                  </ul>
                </div>
              )}
              {(evaluation.improvements.length > 0 || evaluation.missingConcepts.length > 0) && (
                <div className="border border-[#f3c969]/25 bg-[#f3c969]/5 p-3">
                  <span className="mono text-[10px] font-bold uppercase text-[#f3c969]">Improvements &amp; Missed Concepts</span>
                  <ul className="mt-1.5 list-disc space-y-1 pl-4 text-[#c8cbd3]">
                    {evaluation.improvements.map((imp, i) => <li key={i}>{imp}</li>)}
                  </ul>
                </div>
              )}
            </div>
          )}

          {evaluation?.referenceAnswer && (
            <div className="border border-white/[.08] bg-[#15181f] p-3.5">
              <span className="mono text-[10px] uppercase tracking-wider text-[#89a96b]">Reference Model Answer</span>
              <p className="mt-1.5 leading-5 text-[#c8cbd3]">{evaluation.referenceAnswer}</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

function FinalReportSection({ session }: { session: AiInterviewSession }) {
  const report = session.finalReport!
  return (
    <section className="space-y-6 border border-[#3b3523] bg-[#11141a] p-5 sm:p-7">
      <div className="flex flex-col justify-between gap-4 border-b border-white/[.08] pb-5 sm:flex-row sm:items-center">
        <div>
          <span className="mono text-[10px] uppercase tracking-[.22em] text-[#f3c969]">Complete AI Interview Report</span>
          <h2 className="mt-1.5 text-2xl font-extrabold tracking-tight text-white">
            Final Evaluation &amp; Eye-Proctoring Verdict
          </h2>
        </div>
        <div className="flex items-center gap-3">
          <div className="border border-[#f3c969]/40 bg-[#242015] px-4 py-2 text-center">
            <span className="mono block text-[9px] text-[#c6b776]">OVERALL SCORE</span>
            <span className="mono text-2xl font-extrabold text-[#f3c969]">{report.overallScore}/100</span>
          </div>
          <div className="border border-white/[.14] bg-[#0d0f14] px-4 py-2 text-center">
            <span className="mono block text-[9px] text-[#858a98]">RECOMMENDATION</span>
            <span className="mono text-sm font-extrabold text-white">{report.recommendation.replace('_', ' ')}</span>
          </div>
        </div>
      </div>

      <p className="text-sm leading-6 text-[#d5d7df]">{report.executiveSummary}</p>

      <div className="grid gap-3 sm:grid-cols-5">
        <MiniMeter label="Technical Depth" value={report.technicalDepthScore} />
        <MiniMeter label="Problem Solving" value={report.problemSolvingScore} />
        <MiniMeter label="Communication" value={report.communicationScore} />
        <MiniMeter label="Consistency" value={report.consistencyScore} />
        <MiniMeter label="Focus Integrity" value={report.integrityFocusScore} />
      </div>

      <div className="border border-white/[.08] bg-[#0d0f14] p-4">
        <div className="flex items-center gap-2 text-xs font-extrabold text-[#89a96b]">
          <TrendingUp size={15} /> Complete Eye &amp; Integrity Monitoring Verdict
        </div>
        <p className="mt-1.5 text-xs leading-5 text-[#c8cbd3]">{report.monitoringVerdict}</p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div className="border border-[#89a96b]/25 bg-[#89a96b]/5 p-4">
          <h3 className="mono text-[10px] font-bold uppercase tracking-wider text-[#89a96b]">Top Strengths</h3>
          <ul className="mt-2.5 list-disc space-y-1.5 pl-4 text-xs text-[#d5d7df]">
            {report.topStrengths.map((item, i) => <li key={i}>{item}</li>)}
          </ul>
        </div>
        <div className="border border-[#f3c969]/25 bg-[#f3c969]/5 p-4">
          <h3 className="mono text-[10px] font-bold uppercase tracking-wider text-[#f3c969]">Areas to Improve</h3>
          <ul className="mt-2.5 list-disc space-y-1.5 pl-4 text-xs text-[#d5d7df]">
            {report.improvementAreas.map((item, i) => <li key={i}>{item}</li>)}
          </ul>
        </div>
        <div className="border border-white/[.1] bg-[#0d0f14] p-4">
          <h3 className="mono text-[10px] font-bold uppercase tracking-wider text-white">Actionable Study Plan</h3>
          <ul className="mt-2.5 list-disc space-y-1.5 pl-4 text-xs text-[#c8cbd3]">
            {report.studyPlan.map((item, i) => <li key={i}>{item}</li>)}
          </ul>
        </div>
      </div>

      <div className="flex justify-end pt-2">
        <Link
          to="/ai-interviews"
          className="bg-[#f3c969] px-5 py-2.5 text-xs font-extrabold text-[#17140d] hover:bg-[#e0b757]"
        >
          Start Another AI Practice Session
        </Link>
      </div>
    </section>
  )
}

function MiniMeter({ label, value }: { label: string; value: number }) {
  const pct = Math.max(0, Math.min(100, value ?? 0))
  return (
    <div className="border border-white/[.08] bg-[#0d0f14] p-2.5">
      <div className="flex items-center justify-between text-[11px]">
        <span className="text-[#858a98]">{label}</span>
        <span className="mono font-bold text-white">{pct}%</span>
      </div>
      <div className="mt-1.5 h-1.5 w-full bg-[#1a1d26]">
        <div
          className={`h-full ${pct >= 65 ? 'bg-[#89a96b]' : pct >= 40 ? 'bg-[#f3c969]' : 'bg-[#e56b5d]'}`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}
