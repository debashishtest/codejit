import { useEffect, useRef, useState } from 'react'
import {
  Camera,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Copy,
  Layers,
  Mic,
  MicOff,
  MonitorUp,
  PhoneOff,
  Play,
  Plus,
  Send,
  Sparkles,
  Terminal,
  Trash2,
  Users,
  Video,
  VideoOff,
  Wifi,
  XCircle
} from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { api, type CustomTestCase, type Interview, type LiveInterviewEvent, type TestResult } from '../api'
import { useAuth } from '../auth/AuthContext'
import { notify } from '../notifications/notificationBus'

const wsUrl = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8080/ws/interviews'

export function LiveInterviewPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { username } = useAuth()

  const [room, setRoom] = useState<Interview | null>(null)
  const [board, setBoard] = useState('')
  const [code, setCode] = useState('')
  const [language, setLanguage] = useState<'java' | 'python'>('java')
  const [messages, setMessages] = useState<Array<{ sender: string; text: string; time: string }>>([])
  const [chat, setChat] = useState('')
  const [connected, setConnected] = useState(false)

  // Media States
  const [muted, setMuted] = useState(false)
  const [cameraActive, setCameraActive] = useState(false)
  const [screenSharing, setScreenSharing] = useState(false)

  // Execution & Test Case States
  const [testCases, setTestCases] = useState<CustomTestCase[]>([
    { id: '1', sequence: 0, input: '5', expectedOutput: '25' },
    { id: '2', sequence: 1, input: '10', expectedOutput: '100' }
  ])
  const [runningCode, setRunningCode] = useState(false)
  const [testResults, setTestResults] = useState<TestResult[] | null>(null)
  const [activeTab, setActiveTab] = useState<'editor' | 'board' | 'tests'>('editor')
  const [showAddTest, setShowAddTest] = useState(false)
  const [newTestInput, setNewTestInput] = useState('')
  const [newTestExpected, setNewTestExpected] = useState('')
  const [consoleOpen, setConsoleOpen] = useState(true)

  // Refs
  const socket = useRef<WebSocket | null>(null)
  const cameraVideoRef = useRef<HTMLVideoElement>(null)
  const screenVideoRef = useRef<HTMLVideoElement>(null)
  const localStreamRef = useRef<MediaStream | null>(null)
  const screenStreamRef = useRef<MediaStream | null>(null)

  // Determine user role
  const isInterviewer = Boolean(
    room && (
      (room.hostEmail && username && room.hostEmail.toLowerCase() === username.toLowerCase()) ||
      room.participants?.some(p => p.username?.toLowerCase() === username?.toLowerCase() && p.role === 'INTERVIEWER')
    )
  )

  // 1. Fetch Room Details
  useEffect(() => {
    if (!id) return
    api.interview(Number(id))
      .then((val) => {
        setRoom(val)
        if (val.boardSnapshot) setBoard(val.boardSnapshot)
        if (val.editorSnapshot) setCode(val.editorSnapshot)
      })
      .catch((err) => {
        notify({ kind: 'error', title: 'Interview Error', message: err instanceof Error ? err.message : 'Unable to connect' })
        navigate('/interviews')
      })
  }, [id, navigate])

  // 2. Setup STOMP WebSocket Connection
  useEffect(() => {
    if (!room || !id) return
    const token = localStorage.getItem('forge_token')
    const ws = new WebSocket(wsUrl)
    socket.current = ws

    ws.onopen = () => {
      ws.send(`CONNECT\nAuthorization: Bearer ${token}\naccept-version:1.2\n\n\u0000`)
    }

    ws.onmessage = (event) => {
      const raw = String(event.data)
      if (raw.includes('CONNECTED')) {
        setConnected(true)
        ws.send(`SUBSCRIBE\nid:sub-${id}\ndestination:/topic/interviews/${id}\n\n\u0000`)
        return
      }

      const body = raw.split('\n\n')[1]?.replace(/\u0000/g, '')
      if (!body) return

      try {
        const message = JSON.parse(body) as LiveInterviewEvent & { sender?: string; timestamp?: number }
        if (message.type === 'BOARD_UPDATED' && message.payload !== undefined) {
          setBoard(message.payload)
        } else if (message.type === 'EDITOR_UPDATED' && message.payload !== undefined) {
          setCode(message.payload)
        } else if (message.type === 'CHAT_MESSAGE') {
          const time = message.timestamp ? new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          setMessages((prev) => [...prev, { sender: message.sender ?? 'Participant', text: message.payload ?? '', time }])
        } else if (message.type === 'TEST_CASES_UPDATED' && message.payload) {
          try {
            const parsed = JSON.parse(message.payload)
            if (Array.isArray(parsed)) setTestCases(parsed)
          } catch { /* Ignore malformed payload */ }
        } else if (message.type === 'CODE_RUN_RESULT' && message.payload) {
          try {
            const parsed = JSON.parse(message.payload)
            setTestResults(parsed)
            setConsoleOpen(true)
          } catch { /* Ignore */ }
        }
      } catch {
        // Ignore non-json STOMP frames
      }
    }

    ws.onclose = () => setConnected(false)

    return () => {
      ws.close()
      setConnected(false)
      // Stop all media tracks on unmount
      localStreamRef.current?.getTracks().forEach(t => t.stop())
      screenStreamRef.current?.getTracks().forEach(t => t.stop())
    }
  }, [room, id])

  // Publish STOMP event
  const publish = (event: LiveInterviewEvent) => {
    if (!socket.current || socket.current.readyState !== WebSocket.OPEN) return
    socket.current.send(`SEND\ndestination:/app/interviews/${id}/event\ncontent-type:application/json\n\n${JSON.stringify(event)}\u0000`)
  }

  const updateBoard = (val: string) => {
    setBoard(val)
    publish({ type: 'BOARD_UPDATED', payload: val })
  }

  const updateCode = (val: string) => {
    setCode(val)
    publish({ type: 'EDITOR_UPDATED', payload: val })
  }

  const sendChat = () => {
    if (!chat.trim()) return
    const text = chat.trim()
    publish({ type: 'CHAT_MESSAGE', payload: text })
    setMessages((prev) => [...prev, { sender: 'You', text, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }])
    setChat('')
  }

  // 3. Media Controls (Camera & Screen Share)
  const toggleCamera = async () => {
    if (cameraActive && localStreamRef.current) {
      localStreamRef.current.getTracks().forEach(t => t.stop())
      localStreamRef.current = null
      if (cameraVideoRef.current) cameraVideoRef.current.srcObject = null
      setCameraActive(false)
      return
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: !muted })
      localStreamRef.current = stream
      if (cameraVideoRef.current) {
        cameraVideoRef.current.srcObject = stream
        await cameraVideoRef.current.play()
      }
      setCameraActive(true)
      notify({ kind: 'info', title: 'Camera Enabled', message: 'Your video is now broadcasting in the room.' })
    } catch (err) {
      setCameraActive(false)
      notify({ kind: 'error', title: 'Camera Error', message: 'Unable to access webcam. Please check permissions.' })
    }
  }

  const toggleScreenShare = async () => {
    if (screenSharing && screenStreamRef.current) {
      screenStreamRef.current.getTracks().forEach(t => t.stop())
      screenStreamRef.current = null
      if (screenVideoRef.current) screenVideoRef.current.srcObject = null
      setScreenSharing(false)
      notify({ kind: 'info', title: 'Screen Share Ended', message: 'You stopped sharing your screen.' })
      return
    }

    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: false
      })
      screenStreamRef.current = stream

      if (screenVideoRef.current) {
        screenVideoRef.current.srcObject = stream
        await screenVideoRef.current.play()
      }

      setScreenSharing(true)
      notify({ kind: 'success', title: 'Screen Sharing Active', message: 'Your screen is now visible to participants.' })

      // Handle user clicking native browser "Stop sharing" button
      stream.getVideoTracks()[0].onended = () => {
        setScreenSharing(false)
        screenStreamRef.current = null
        if (screenVideoRef.current) screenVideoRef.current.srcObject = null
      }
    } catch (err) {
      setScreenSharing(false)
      // If user cancelled, don't trigger error toast
      if ((err as Error).name !== 'NotAllowedError') {
        notify({ kind: 'error', title: 'Screen Share Error', message: 'Unable to capture screen display.' })
      }
    }
  }

  const toggleMute = () => {
    const next = !muted
    setMuted(next)
    if (localStreamRef.current) {
      localStreamRef.current.getAudioTracks().forEach(track => {
        track.enabled = !next
      })
    }
  }

  // 4. Test Case Operations
  const addTestCase = () => {
    if (!newTestInput.trim() && !newTestExpected.trim()) {
      notify({ kind: 'error', title: 'Validation Error', message: 'Input or expected output is required.' })
      return
    }

    const nextCases: CustomTestCase[] = [
      ...testCases,
      {
        id: Date.now().toString(),
        sequence: testCases.length,
        input: newTestInput,
        expectedOutput: newTestExpected
      }
    ]

    setTestCases(nextCases)
    publish({ type: 'TEST_CASES_UPDATED', payload: JSON.stringify(nextCases) })
    setNewTestInput('')
    setNewTestExpected('')
    setShowAddTest(false)
    notify({ kind: 'success', title: 'Test Case Added', message: `Test #${nextCases.length} configured.` })
  }

  const removeTestCase = (index: number) => {
    const nextCases = testCases.filter((_, i) => i !== index).map((tc, idx) => ({ ...tc, sequence: idx }))
    setTestCases(nextCases)
    publish({ type: 'TEST_CASES_UPDATED', payload: JSON.stringify(nextCases) })
  }

  // 5. Code Execution Engine
  const handleRunCode = async () => {
    if (!code.trim()) {
      notify({ kind: 'error', title: 'Empty Code', message: 'Please enter code before running.' })
      return
    }

    setRunningCode(true)
    setConsoleOpen(true)
    try {
      const response = await api.executeCode({
        sourceCode: code,
        language,
        testCases: testCases.map(tc => ({
          sequence: tc.sequence,
          input: tc.input,
          expectedOutput: tc.expectedOutput
        }))
      })

      setTestResults(response.results)
      publish({ type: 'CODE_RUN_RESULT', payload: JSON.stringify(response.results) })

      const allPassed = response.results.every(r => r.passed)
      if (allPassed) {
        notify({ kind: 'success', title: 'All Tests Passed!', message: `Execution finished in ${response.totalRuntimeMillis ?? 0}ms.` })
      } else {
        notify({ kind: 'error', title: 'Tests Failed', message: 'One or more test cases did not match expected output.' })
      }
    } catch (err) {
      notify({ kind: 'error', title: 'Execution Error', message: err instanceof Error ? err.message : 'Execution failed' })
    } finally {
      setRunningCode(false)
    }
  }

  if (!room) {
    return (
      <div className="flex min-h-[calc(100vh-76px)] items-center justify-center bg-[#0b0d11]">
        <div className="flex items-center gap-3 border border-white/[.08] bg-[#11141a] px-6 py-4 text-sm text-[#858a98]">
          <div className="h-4 w-4 animate-spin rounded-full border-2 border-[#f3c969] border-t-transparent" />
          <span>Connecting to CodeJIT live interview workspace...</span>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-[calc(100vh-76px)] bg-[#0b0d11] p-3 sm:p-5">
      <div className="mx-auto max-w-[1600px] space-y-4">
        {/* Header Bar */}
        <header className="flex flex-wrap items-center justify-between gap-4 border border-white/[.08] bg-[#11141a] px-5 py-4">
          <div className="flex flex-wrap items-center gap-4">
            <div>
              <div className="flex items-center gap-2">
                <span className={`h-2.5 w-2.5 rounded-full ${connected ? 'bg-[#89a96b] shadow-[0_0_8px_#89a96b]' : 'bg-[#e56b5d]'}`} />
                <span className="mono text-[11px] font-bold uppercase tracking-[.18em] text-[#f3c969]">
                  {connected ? 'LIVE SYNC ACTIVE' : 'CONNECTING...'}
                </span>
                <span className={`ml-2 px-2 py-0.5 text-[10px] font-extrabold uppercase tracking-wider ${isInterviewer ? 'bg-[#f3c969]/20 text-[#f3c969] border border-[#f3c969]/40' : 'bg-blue-500/20 text-blue-400 border border-blue-500/40'}`}>
                  {isInterviewer ? 'Interviewer (Host)' : 'Candidate'}
                </span>
              </div>
              <h1 className="mt-1 text-xl font-extrabold tracking-tight text-white">{room.title}</h1>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <div className="flex items-center gap-2 border border-white/[.1] bg-[#0d0f14] px-3 py-1.5 text-xs text-[#c8cbd3]">
              <span className="mono text-[#f3c969] font-bold">CODE: {room.shareCode}</span>
              <button
                onClick={() => {
                  navigator.clipboard?.writeText(room.shareCode)
                  notify({ kind: 'info', title: 'Code Copied', message: `Share code ${room.shareCode} copied to clipboard.` })
                }}
                className="text-[#858a98] hover:text-white"
                title="Copy share code"
              >
                <Copy size={13} />
              </button>
            </div>

            <div className="flex items-center gap-2 border border-white/[.1] bg-[#0d0f14] px-3 py-1.5 text-xs text-[#858a98]">
              <Users size={14} className="text-[#f3c969]" />
              <span>{room.participants?.length ?? 1} in room</span>
            </div>

            <button
              onClick={() => navigate('/interviews')}
              className="flex items-center gap-2 bg-[#e56b5d] px-3 py-2 text-xs font-extrabold text-white transition hover:bg-[#d8584a]"
            >
              <PhoneOff size={14} /> Leave
            </button>
          </div>
        </header>

        {/* Screen Share Spotlight (Visible when active) */}
        {screenSharing && (
          <section className="relative overflow-hidden border border-[#89a96b]/40 bg-[#0d0f14]">
            <div className="flex items-center justify-between border-b border-white/[.08] bg-[#15181f] px-4 py-2 text-xs">
              <span className="flex items-center gap-2 font-bold text-[#89a96b]">
                <MonitorUp size={14} /> Screen Share Broadcast
              </span>
              <button
                onClick={toggleScreenShare}
                className="bg-[#e56b5d] px-2 py-1 text-[11px] font-bold text-white hover:bg-[#d8584a]"
              >
                Stop Sharing
              </button>
            </div>
            <div className="relative aspect-video max-h-[480px] w-full bg-black">
              <video ref={screenVideoRef} autoPlay playsInline className="h-full w-full object-contain" />
            </div>
          </section>
        )}

        {/* Workspace Layout */}
        <div className="grid gap-4 lg:grid-cols-[1fr_340px]">
          {/* Main Work Area: Editor & Shared Board */}
          <div className="space-y-4">
            {/* Tabs for Editor vs Board vs Tests */}
            <div className="flex border-b border-white/[.08] bg-[#11141a]">
              <button
                onClick={() => setActiveTab('editor')}
                className={`flex items-center gap-2 px-5 py-3 text-xs font-extrabold tracking-wide uppercase transition ${activeTab === 'editor' ? 'border-b-2 border-[#f3c969] bg-[#15181f] text-white' : 'text-[#858a98] hover:text-white'}`}
              >
                <Terminal size={14} className="text-[#f3c969]" /> Code Editor
              </button>
              <button
                onClick={() => setActiveTab('board')}
                className={`flex items-center gap-2 px-5 py-3 text-xs font-extrabold tracking-wide uppercase transition ${activeTab === 'board' ? 'border-b-2 border-[#f3c969] bg-[#15181f] text-white' : 'text-[#858a98] hover:text-white'}`}
              >
                <Layers size={14} className="text-[#f3c969]" /> Shared Whiteboard & Notes
              </button>
              <button
                onClick={() => setActiveTab('tests')}
                className={`flex items-center gap-2 px-5 py-3 text-xs font-extrabold tracking-wide uppercase transition ${activeTab === 'tests' ? 'border-b-2 border-[#f3c969] bg-[#15181f] text-white' : 'text-[#858a98] hover:text-white'}`}
              >
                <Sparkles size={14} className="text-[#f3c969]" /> Test Cases ({testCases.length})
              </button>
            </div>

            {/* Tab 1: Code Editor */}
            {activeTab === 'editor' && (
              <section className="border border-white/[.08] bg-[#11141a]">
                <div className="flex flex-wrap items-center justify-between border-b border-white/[.08] px-4 py-2.5 text-xs">
                  <div className="flex items-center gap-3">
                    <select
                      value={language}
                      onChange={(e) => setLanguage(e.target.value as 'java' | 'python')}
                      className="border border-white/[.12] bg-[#0d0f14] px-2.5 py-1 text-xs font-bold text-[#f3c969] outline-none"
                    >
                      <option value="java">Java 21</option>
                      <option value="python">Python 3</option>
                    </select>
                    <span className="mono text-[10px] text-[#686d7a]">REAL-TIME SYNC</span>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => {
                        navigator.clipboard?.writeText(code)
                        notify({ kind: 'info', title: 'Code Copied', message: 'Editor content copied to clipboard.' })
                      }}
                      className="flex items-center gap-1 border border-white/[.1] px-2.5 py-1 text-[11px] text-[#c8cbd3] hover:text-white"
                    >
                      <Copy size={12} /> Copy
                    </button>
                    <button
                      onClick={handleRunCode}
                      disabled={runningCode}
                      className="flex items-center gap-2 bg-[#f3c969] px-4 py-1.5 text-xs font-extrabold text-[#17140d] transition hover:bg-[#e0b757] disabled:opacity-50"
                    >
                      <Play size={13} fill="currentColor" /> {runningCode ? 'Running Sandbox...' : 'Run Code'}
                    </button>
                  </div>
                </div>

                <textarea
                  value={code}
                  onChange={(e) => updateCode(e.target.value)}
                  placeholder="// Write or paste interview solution here..."
                  spellCheck={false}
                  className="mono min-h-[380px] w-full resize-y bg-[#0d0f14] p-4 text-xs font-mono leading-6 text-[#d5d7df] outline-none focus:bg-[#0f1217]"
                />

                {/* Integrated Test Results & Terminal Console */}
                <div className="border-t border-white/[.08] bg-[#15181f]">
                  <div
                    onClick={() => setConsoleOpen(!consoleOpen)}
                    className="flex cursor-pointer items-center justify-between px-4 py-2.5 text-xs font-bold text-[#c8cbd3] hover:bg-white/[.02]"
                  >
                    <span className="flex items-center gap-2">
                      <Terminal size={14} className="text-[#f3c969]" /> Execution Console & Verdicts
                    </span>
                    {consoleOpen ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
                  </div>

                  {consoleOpen && (
                    <div className="border-t border-white/[.06] p-4 space-y-3 bg-[#0d0f14]">
                      {runningCode ? (
                        <div className="flex items-center gap-2 text-xs text-[#858a98]">
                          <div className="h-3 w-3 animate-spin rounded-full border-2 border-[#f3c969] border-t-transparent" />
                          <span>Compiling and executing in isolated container sandbox...</span>
                        </div>
                      ) : testResults ? (
                        <div className="space-y-2">
                          <div className="flex items-center justify-between pb-2 border-b border-white/[.06]">
                            <span className="text-xs font-bold text-white">
                              {testResults.every(r => r.passed) ? (
                                <span className="text-[#89a96b] flex items-center gap-1.5">
                                  <CheckCircle2 size={14} /> All {testResults.length} test cases passed
                                </span>
                              ) : (
                                <span className="text-[#e56b5d] flex items-center gap-1.5">
                                  <XCircle size={14} /> {testResults.filter(r => !r.passed).length} / {testResults.length} test cases failed
                                </span>
                              )}
                            </span>
                          </div>

                          <div className="grid gap-2">
                            {testResults.map((result, idx) => (
                              <div
                                key={idx}
                                className={`border p-3 text-xs mono ${result.passed ? 'border-[#89a96b]/30 bg-[#89a96b]/5' : 'border-[#e56b5d]/30 bg-[#e56b5d]/5'}`}
                              >
                                <div className="flex items-center justify-between mb-1.5">
                                  <span className="font-bold text-white">Test Case #{idx + 1}</span>
                                  <span className="text-[10px] text-[#858a98]">{result.runtimeMillis}ms</span>
                                </div>
                                {result.errorMessage && (
                                  <div className="mb-2 text-[#e56b5d] text-[11px] whitespace-pre-wrap">
                                    {result.errorMessage}
                                  </div>
                                )}
                                <div className="grid grid-cols-2 gap-2 text-[11px]">
                                  <div>
                                    <span className="text-[#858a98] block">Expected:</span>
                                    <pre className="bg-[#000]/40 p-1.5 mt-0.5 text-[#89a96b] overflow-x-auto">{result.expectedOutput || '(empty)'}</pre>
                                  </div>
                                  <div>
                                    <span className="text-[#858a98] block">Actual Output:</span>
                                    <pre className="bg-[#000]/40 p-1.5 mt-0.5 text-white overflow-x-auto">{result.actualOutput || '(none)'}</pre>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      ) : (
                        <p className="text-xs text-[#686d7a]">
                          Click &quot;Run Code&quot; above to execute and evaluate the solution against configured test cases.
                        </p>
                      )}
                    </div>
                  )}
                </div>
              </section>
            )}

            {/* Tab 2: Shared Whiteboard */}
            {activeTab === 'board' && (
              <section className="border border-white/[.08] bg-[#11141a]">
                <div className="flex items-center justify-between border-b border-white/[.08] px-4 py-3">
                  <div className="flex items-center gap-2">
                    <Layers size={15} className="text-[#f3c969]" />
                    <span className="text-xs font-extrabold text-white">Shared Whiteboard & Problem Description</span>
                  </div>
                  <span className="mono text-[10px] text-[#89a96b]">LIVE BROADCAST</span>
                </div>
                <textarea
                  value={board}
                  onChange={(e) => updateBoard(e.target.value)}
                  placeholder="Paste problem statements, hints, algorithmic diagrams, or meeting notes here..."
                  className="min-h-[480px] w-full resize-y bg-[#0d0f14] p-5 text-sm leading-6 text-[#c8cbd3] outline-none focus:bg-[#10131a]"
                />
              </section>
            )}

            {/* Tab 3: Dynamic Test Cases Management */}
            {activeTab === 'tests' && (
              <section className="border border-white/[.08] bg-[#11141a] p-5 space-y-4">
                <div className="flex items-center justify-between border-b border-white/[.08] pb-3">
                  <div>
                    <h3 className="text-sm font-extrabold text-white">Interview Test Cases</h3>
                    <p className="text-xs text-[#858a98]">Define inputs and expected outputs to validate candidate code.</p>
                  </div>
                  <button
                    onClick={() => setShowAddTest(!showAddTest)}
                    className="flex items-center gap-1.5 bg-[#f3c969] px-3 py-1.5 text-xs font-extrabold text-[#17140d]"
                  >
                    <Plus size={14} /> Add Test Case
                  </button>
                </div>

                {showAddTest && (
                  <div className="border border-[#f3c969]/40 bg-[#181710] p-4 space-y-3">
                    <h4 className="text-xs font-bold text-[#f3c969]">New Test Case</h4>
                    <div className="grid gap-3 sm:grid-cols-2">
                      <div>
                        <label className="text-[11px] text-[#858a98] block mb-1">Standard Input (stdin)</label>
                        <textarea
                          value={newTestInput}
                          onChange={(e) => setNewTestInput(e.target.value)}
                          placeholder="e.g. 4 5"
                          className="mono w-full bg-[#0d0f14] border border-white/[.12] p-2 text-xs text-white outline-none"
                          rows={3}
                        />
                      </div>
                      <div>
                        <label className="text-[11px] text-[#858a98] block mb-1">Expected Output (stdout)</label>
                        <textarea
                          value={newTestExpected}
                          onChange={(e) => setNewTestExpected(e.target.value)}
                          placeholder="e.g. 20"
                          className="mono w-full bg-[#0d0f14] border border-white/[.12] p-2 text-xs text-white outline-none"
                          rows={3}
                        />
                      </div>
                    </div>
                    <div className="flex justify-end gap-2 pt-2">
                      <button
                        onClick={() => setShowAddTest(false)}
                        className="px-3 py-1.5 text-xs text-[#858a98] hover:text-white"
                      >
                        Cancel
                      </button>
                      <button
                        onClick={addTestCase}
                        className="bg-[#f3c969] px-3 py-1.5 text-xs font-extrabold text-[#17140d]"
                      >
                        Save Test Case
                      </button>
                    </div>
                  </div>
                )}

                <div className="space-y-3">
                  {testCases.map((tc, idx) => (
                    <div key={tc.id ?? idx} className="border border-white/[.08] bg-[#0d0f14] p-4">
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-xs font-bold text-[#f3c969]">Test #{idx + 1}</span>
                        <button
                          onClick={() => removeTestCase(idx)}
                          className="text-[#858a98] hover:text-[#e56b5d]"
                          title="Delete test case"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                      <div className="grid sm:grid-cols-2 gap-3 text-xs mono">
                        <div>
                          <span className="text-[10px] text-[#858a98] block">INPUT</span>
                          <pre className="bg-[#15181f] p-2 mt-1 text-[#d5d7df] overflow-x-auto">{tc.input || '(empty)'}</pre>
                        </div>
                        <div>
                          <span className="text-[10px] text-[#858a98] block">EXPECTED OUTPUT</span>
                          <pre className="bg-[#15181f] p-2 mt-1 text-[#89a96b] overflow-x-auto">{tc.expectedOutput || '(empty)'}</pre>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}
          </div>

          {/* Right Sidebar: Video & Room Chat */}
          <aside className="space-y-4">
            {/* Video & Media Controls */}
            <section className="border border-white/[.08] bg-[#11141a] p-3 space-y-3">
              <div className="flex items-center justify-between px-1">
                <span className="text-xs font-extrabold text-white">Video & Collaboration</span>
                <span className="flex items-center gap-1 text-[10px] text-[#89a96b]">
                  <Wifi size={11} /> Secure Room
                </span>
              </div>

              {/* Local Camera Tile */}
              <div className="relative aspect-video overflow-hidden bg-[#181b22] border border-white/[.06]">
                <video
                  ref={cameraVideoRef}
                  autoPlay
                  playsInline
                  muted
                  className={`h-full w-full object-cover ${cameraActive ? '' : 'hidden'}`}
                />
                {!cameraActive && (
                  <div className="flex h-full w-full flex-col items-center justify-center text-[#686d7a] gap-2">
                    <VideoOff size={24} />
                    <span className="text-[11px]">Camera is off</span>
                  </div>
                )}
                <span className="absolute bottom-2 left-2 bg-black/60 px-2 py-0.5 text-[10px] text-white">
                  You ({isInterviewer ? 'Interviewer' : 'Candidate'})
                </span>
              </div>

              {/* Media Control Toolbar */}
              <div className="flex items-center justify-center gap-3 pt-1">
                <button
                  onClick={toggleMute}
                  aria-label={muted ? 'Unmute microphone' : 'Mute microphone'}
                  className={`grid h-10 w-10 place-items-center border transition ${muted ? 'bg-[#e56b5d]/20 border-[#e56b5d] text-[#e56b5d]' : 'border-white/[.15] text-[#d5d7df] hover:border-[#f3c969]'}`}
                  title={muted ? 'Unmute' : 'Mute'}
                >
                  {muted ? <MicOff size={16} /> : <Mic size={16} />}
                </button>

                <button
                  onClick={toggleCamera}
                  aria-label={cameraActive ? 'Turn off camera' : 'Turn on camera'}
                  className={`grid h-10 w-10 place-items-center border transition ${cameraActive ? 'bg-[#89a96b]/20 border-[#89a96b] text-[#89a96b]' : 'border-white/[.15] text-[#d5d7df] hover:border-[#f3c969]'}`}
                  title={cameraActive ? 'Disable Camera' : 'Enable Camera'}
                >
                  {cameraActive ? <Video size={16} /> : <Camera size={16} />}
                </button>

                <button
                  onClick={toggleScreenShare}
                  aria-label={screenSharing ? 'Stop screen share' : 'Share screen'}
                  className={`grid h-10 w-10 place-items-center border transition ${screenSharing ? 'bg-[#89a96b] text-[#0b0d11] font-bold border-[#89a96b]' : 'border-white/[.15] text-[#d5d7df] hover:border-[#f3c969]'}`}
                  title={screenSharing ? 'Stop Sharing Screen' : 'Share Screen'}
                >
                  <MonitorUp size={16} />
                </button>
              </div>
            </section>

            {/* Room Chat Section */}
            <section className="flex min-h-[320px] flex-col border border-white/[.08] bg-[#11141a]">
              <div className="flex items-center justify-between border-b border-white/[.08] px-4 py-3">
                <span className="text-xs font-extrabold text-white">Live Room Chat</span>
                <span className="mono text-[10px] text-[#858a98]">{messages.length} messages</span>
              </div>

              <div className="flex-1 space-y-3 overflow-y-auto p-4 max-h-[360px]">
                {messages.length === 0 ? (
                  <p className="text-xs text-[#686d7a] text-center pt-8">
                    Start a conversation with other interview participants here.
                  </p>
                ) : (
                  messages.map((message, index) => (
                    <div key={`${message.sender}-${index}`} className="space-y-0.5">
                      <div className="flex items-center justify-between">
                        <span className={`text-[10px] font-bold ${message.sender === 'You' ? 'text-[#89a96b]' : 'text-[#f3c969]'}`}>
                          {message.sender}
                        </span>
                        <span className="text-[9px] text-[#686d7a]">{message.time}</span>
                      </div>
                      <p className="text-xs leading-5 text-[#c1c4cc] bg-[#0d0f14] p-2 border border-white/[.04]">
                        {message.text}
                      </p>
                    </div>
                  ))
                )}
              </div>

              <div className="flex border-t border-white/[.08] bg-[#0d0f14] p-2">
                <input
                  value={chat}
                  onChange={(e) => setChat(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && sendChat()}
                  placeholder="Type a message..."
                  className="min-w-0 flex-1 bg-transparent px-3 py-1.5 text-xs text-white outline-none"
                />
                <button
                  onClick={sendChat}
                  aria-label="Send message"
                  className="px-2 text-[#f3c969] hover:text-white"
                >
                  <Send size={15} />
                </button>
              </div>
            </section>
          </aside>
        </div>
      </div>
    </div>
  )
}
