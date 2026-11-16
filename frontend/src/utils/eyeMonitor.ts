export type LocalEyeObservation =
  | 'EYES_ON_SCREEN'
  | 'GAZE_AWAY'
  | 'FACE_MISSING'
  | 'MULTIPLE_FACES'
  | 'CAMERA_COVERED'

export type LocalEyeAnalysis = {
  observation: LocalEyeObservation
  facePresent: boolean
  multiplePeople: boolean
  lookingAtScreen: boolean
  gazeDirection: 'CENTER' | 'LEFT' | 'RIGHT' | 'DOWN' | 'AWAY'
  reason?: string
}

type DetectedFaceLike = {
  boundingBox: { x: number; y: number; width: number; height: number }
  landmarks?: Array<{ type: string; locations: Array<{ x: number; y: number }> }>
}

type FaceDetectorLike = {
  detect: (image: HTMLVideoElement) => Promise<DetectedFaceLike[]>
}

let cachedFaceDetector: FaceDetectorLike | null | undefined

function getFaceDetector(): FaceDetectorLike | null {
  if (cachedFaceDetector !== undefined) return cachedFaceDetector
  if (typeof window === 'undefined') {
    cachedFaceDetector = null
    return null
  }
  const win = window as unknown as {
    FaceDetector?: new (opts?: { fastMode?: boolean; maxDetectedFaces?: number }) => FaceDetectorLike
  }
  if (typeof win.FaceDetector === 'function') {
    try {
      cachedFaceDetector = new win.FaceDetector({ fastMode: true, maxDetectedFaces: 3 })
      return cachedFaceDetector
    } catch {
      cachedFaceDetector = null
      return null
    }
  }
  cachedFaceDetector = null
  return null
}

function isSkinPixel(r: number, g: number, b: number): boolean {
  // YCbCr skin-tone cluster bounds (works well across varied skin tones and indoor lighting)
  const y = 0.299 * r + 0.587 * g + 0.114 * b
  const cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b
  const cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b
  return y > 45 && cb >= 75 && cb <= 132 && cr >= 130 && cr <= 178
}

function computeEyeHorizontalIrisOffset(
  data: Uint8ClampedArray,
  frameW: number,
  frameH: number,
  boxX: number,
  boxY: number,
  boxW: number,
  boxH: number
): number | null {
  const x0 = Math.max(0, Math.floor(boxX))
  const y0 = Math.max(0, Math.floor(boxY))
  const x1 = Math.min(frameW - 1, Math.floor(boxX + boxW))
  const y1 = Math.min(frameH - 1, Math.floor(boxY + boxH))
  if (x1 - x0 < 4 || y1 - y0 < 3) return null

  let weightedX = 0
  let totalDarkness = 0
  for (let y = y0; y <= y1; y++) {
    for (let x = x0; x <= x1; x++) {
      const idx = (y * frameW + x) * 4
      const lum = 0.299 * data[idx] + 0.587 * data[idx + 1] + 0.114 * data[idx + 2]
      const darkness = Math.max(0, 255 - lum)
      const weight = darkness * darkness
      weightedX += (x - x0) * weight
      totalDarkness += weight
    }
  }
  if (totalDarkness <= 0) return null
  return weightedX / totalDarkness / (x1 - x0)
}

export async function analyzeVideoFrameForEyeTracking(
  video: HTMLVideoElement,
  canvas: HTMLCanvasElement
): Promise<LocalEyeAnalysis> {
  if (video.readyState < 2 || video.videoWidth === 0 || video.videoHeight === 0) {
    return {
      observation: 'EYES_ON_SCREEN',
      facePresent: true,
      multiplePeople: false,
      lookingAtScreen: true,
      gazeDirection: 'CENTER',
    }
  }

  const w = 160
  const h = 120
  canvas.width = w
  canvas.height = h
  const ctx = canvas.getContext('2d', { willReadFrequently: true })
  if (!ctx) {
    return {
      observation: 'EYES_ON_SCREEN',
      facePresent: true,
      multiplePeople: false,
      lookingAtScreen: true,
      gazeDirection: 'CENTER',
    }
  }

  ctx.drawImage(video, 0, 0, w, h)
  const imgData = ctx.getImageData(0, 0, w, h).data

  // 1. Check for covered / dark / static camera
  let sumLum = 0
  let sumLumSq = 0
  const pixelCount = w * h
  for (let i = 0; i < imgData.length; i += 4) {
    const lum = 0.299 * imgData[i] + 0.587 * imgData[i + 1] + 0.114 * imgData[i + 2]
    sumLum += lum
    sumLumSq += lum * lum
  }
  const meanLum = sumLum / pixelCount
  const variance = sumLumSq / pixelCount - meanLum * meanLum
  if (meanLum < 16 || variance < 20) {
    return {
      observation: 'CAMERA_COVERED',
      facePresent: false,
      multiplePeople: false,
      lookingAtScreen: false,
      gazeDirection: 'AWAY',
      reason: 'Camera feed appears dark or covered — uncover webcam immediately.',
    }
  }

  // 2. Native browser FaceDetector API when available (Chrome / Edge)
  const detector = getFaceDetector()
  if (detector) {
    try {
      const faces = await detector.detect(video)
      if (faces.length === 0) {
        return {
          observation: 'FACE_MISSING',
          facePresent: false,
          multiplePeople: false,
          lookingAtScreen: false,
          gazeDirection: 'AWAY',
          reason: 'Candidate face left the camera view.',
        }
      }
      if (faces.length > 1) {
        return {
          observation: 'MULTIPLE_FACES',
          facePresent: true,
          multiplePeople: true,
          lookingAtScreen: false,
          gazeDirection: 'AWAY',
          reason: 'Multiple people detected in camera view.',
        }
      }
      const box = faces[0].boundingBox
      const scaleX = w / video.videoWidth
      const scaleY = h / video.videoHeight
      const fx = box.x * scaleX
      const fy = box.y * scaleY
      const fw = box.width * scaleX
      const fh = box.height * scaleY
      const cx = (fx + fw / 2) / w
      const cy = (fy + fh / 2) / h

      if (cx < 0.24 || cx > 0.76) {
        const dir = cx < 0.24 ? 'LEFT' : 'RIGHT'
        return {
          observation: 'GAZE_AWAY',
          facePresent: true,
          multiplePeople: false,
          lookingAtScreen: false,
          gazeDirection: dir,
          reason: `Head/eyes turned off-screen (${dir}).`,
        }
      }
      if (cy > 0.78) {
        return {
          observation: 'GAZE_AWAY',
          facePresent: true,
          multiplePeople: false,
          lookingAtScreen: false,
          gazeDirection: 'DOWN',
          reason: 'Candidate appears to be looking down away from the screen.',
        }
      }

      const leftEyeOffset = computeEyeHorizontalIrisOffset(
        imgData, w, h, fx + fw * 0.16, fy + fh * 0.24, fw * 0.28, fh * 0.18
      )
      const rightEyeOffset = computeEyeHorizontalIrisOffset(
        imgData, w, h, fx + fw * 0.56, fy + fh * 0.24, fw * 0.28, fh * 0.18
      )
      if (leftEyeOffset !== null && rightEyeOffset !== null) {
        const avgIris = (leftEyeOffset + rightEyeOffset) / 2
        if (avgIris < 0.26 || avgIris > 0.74) {
          const dir = avgIris < 0.26 ? 'LEFT' : 'RIGHT'
          return {
            observation: 'GAZE_AWAY',
            facePresent: true,
            multiplePeople: false,
            lookingAtScreen: false,
            gazeDirection: dir,
            reason: `Eye gaze shifted sharply ${dir.toLowerCase()} away from interview prompt.`,
          }
        }
      }

      return {
        observation: 'EYES_ON_SCREEN',
        facePresent: true,
        multiplePeople: false,
        lookingAtScreen: true,
        gazeDirection: 'CENTER',
      }
    } catch {
      // Fall through to YCbCr skin/eye CV analysis
    }
  }

  // 3. Universal Canvas YCbCr Face & Eye-Region CV Fallback
  let skinCount = 0
  let sumX = 0
  let sumY = 0
  let minX = w
  let maxX = 0
  let minY = h
  let maxY = 0
  const colHist = new Uint16Array(w)

  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const idx = (y * w + x) * 4
      if (isSkinPixel(imgData[idx], imgData[idx + 1], imgData[idx + 2])) {
        skinCount++
        sumX += x
        sumY += y
        colHist[x]++
        if (x < minX) minX = x
        if (x > maxX) maxX = x
        if (y < minY) minY = y
        if (y > maxY) maxY = y
      }
    }
  }

  if (skinCount < pixelCount * 0.025) {
    return {
      observation: 'FACE_MISSING',
      facePresent: false,
      multiplePeople: false,
      lookingAtScreen: false,
      gazeDirection: 'AWAY',
      reason: 'Candidate face not detected in camera view.',
    }
  }

  // Check for two separated wide skin clusters (potential second person)
  let clusters = 0
  let inCluster = false
  let currentWidth = 0
  for (let x = 0; x < w; x++) {
    if (colHist[x] >= 6) {
      if (!inCluster) {
        inCluster = true
        currentWidth = 1
      } else {
        currentWidth++
      }
    } else if (inCluster) {
      if (currentWidth >= 14) clusters++
      inCluster = false
      currentWidth = 0
    }
  }
  if (inCluster && currentWidth >= 14) clusters++
  if (clusters >= 2) {
    return {
      observation: 'MULTIPLE_FACES',
      facePresent: true,
      multiplePeople: true,
      lookingAtScreen: false,
      gazeDirection: 'AWAY',
      reason: 'Multiple people detected in camera frame.',
    }
  }

  const cx = sumX / skinCount / w
  const cy = sumY / skinCount / h
  if (cx < 0.22 || cx > 0.78) {
    const dir = cx < 0.22 ? 'LEFT' : 'RIGHT'
    return {
      observation: 'GAZE_AWAY',
      facePresent: true,
      multiplePeople: false,
      lookingAtScreen: false,
      gazeDirection: dir,
      reason: `Face and eyes turned off-screen (${dir}).`,
    }
  }
  if (cy > 0.8) {
    return {
      observation: 'GAZE_AWAY',
      facePresent: true,
      multiplePeople: false,
      lookingAtScreen: false,
      gazeDirection: 'DOWN',
      reason: 'Candidate looking down away from the screen.',
    }
  }

  const fw = Math.max(1, maxX - minX)
  const fh = Math.max(1, maxY - minY)
  const leftEyeOffset = computeEyeHorizontalIrisOffset(
    imgData, w, h, minX + fw * 0.16, minY + fh * 0.22, fw * 0.28, fh * 0.18
  )
  const rightEyeOffset = computeEyeHorizontalIrisOffset(
    imgData, w, h, minX + fw * 0.56, minY + fh * 0.22, fw * 0.28, fh * 0.18
  )
  if (leftEyeOffset !== null && rightEyeOffset !== null) {
    const avgIris = (leftEyeOffset + rightEyeOffset) / 2
    if (avgIris < 0.25 || avgIris > 0.75) {
      const dir = avgIris < 0.25 ? 'LEFT' : 'RIGHT'
      return {
        observation: 'GAZE_AWAY',
        facePresent: true,
        multiplePeople: false,
        lookingAtScreen: false,
        gazeDirection: dir,
        reason: `Eye gaze deviated ${dir.toLowerCase()} from screen.`,
      }
    }
  }

  return {
    observation: 'EYES_ON_SCREEN',
    facePresent: true,
    multiplePeople: false,
    lookingAtScreen: true,
    gazeDirection: 'CENTER',
  }
}

export function captureSnapshotBase64(
  video: HTMLVideoElement,
  canvas: HTMLCanvasElement
): string | null {
  if (video.readyState < 2 || video.videoWidth === 0 || video.videoHeight === 0) {
    return null
  }
  canvas.width = 320
  canvas.height = 240
  const ctx = canvas.getContext('2d')
  if (!ctx) return null
  ctx.drawImage(video, 0, 0, 320, 240)
  const dataUrl = canvas.toDataURL('image/jpeg', 0.55)
  const comma = dataUrl.indexOf(',')
  return comma >= 0 ? dataUrl.slice(comma + 1) : dataUrl
}

export function playCheatingAlertTone(): void {
  if (typeof window === 'undefined') return
  const AudioCtx = window.AudioContext ?? (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext
  if (!AudioCtx) return
  try {
    const ctx = new AudioCtx()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'triangle'
    osc.frequency.setValueAtTime(660, ctx.currentTime)
    osc.frequency.setValueAtTime(520, ctx.currentTime + 0.12)
    gain.gain.setValueAtTime(0.08, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.28)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start()
    osc.stop(ctx.currentTime + 0.3)
    osc.onended = () => { void ctx.close() }
  } catch {
    // Ignore autoplay restriction errors
  }
}
