export type AssessmentStatus = 'DRAFT' | 'STARTED' | 'ENDED'

export type AuthResponse = { token: string; username: string }

export type CustomTestCase = {
  id?: string
  sequence: number
  input: string
  expectedOutput: string
  visible?: boolean
}

export type CodeRequest = {
  sourceCode: string
  language?: string
  input?: string
  expectedOutput?: string
  testCases?: CustomTestCase[]
}

export type TestCaseRequest = { input: string; expectedOutput: string; visible: boolean; sequence: number }
export type CodingQuestionRequest = { question: string; questionNumber: number; language: string; starterCode: string; testCases: TestCaseRequest[] }
export type AssessmentRequest = { title: string; description: string; durationMinutes: number; startTime: string; endTime: string; questions: CodingQuestionRequest[] }
export type VisibleTestCase = { sequence: number; input: string; expectedOutput: string }
export type CodingQuestion = { id: number; question: string; questionNumber: number; language: string; starterCode: string; visibleTestCases: VisibleTestCase[] }
export type Assessment = { id: number; title: string; description?: string; shareCode: string; durationMinutes: number; startTime: string; endTime: string; status: AssessmentStatus; questionCount: number; questions?: CodingQuestion[] }
export type AssessmentSummary = { id: number; title: string; durationMinutes: number; status: AssessmentStatus; questionCount: number }
export type TestResult = { sequence: number; passed: boolean; actualOutput: string; expectedOutput?: string; errorMessage?: string; runtimeMillis: number }
export type RunResponse = { results: TestResult[]; totalRuntimeMillis?: number; success?: boolean }
export type SubmissionResponse = { id: number; assessmentId: number; questionId: number; status: 'PASSED' | 'FAILED' | 'ERROR' | 'TIMEOUT'; passedTests: number; totalTests: number; submittedAt: string; results: TestResult[] }
export type InterviewStatus = 'SCHEDULED' | 'LIVE' | 'ENDED' | 'CANCELLED'
export type InterviewParticipant = { userId?: number; username: string; role: 'INTERVIEWER' | 'CANDIDATE'; online: boolean; joinedAt?: string }
export type Interview = { id: number; title: string; description?: string; assessmentId?: number; shareCode: string; scheduledStart: string; scheduledEnd: string; status: InterviewStatus; hostEmail?: string; currentQuestionId?: number; boardSnapshot?: string; editorSnapshot?: string; participants: InterviewParticipant[] }
export type InterviewRequest = { title: string; description: string; assessmentId?: number; scheduledStart: string; scheduledEnd: string }
export type LiveInterviewEvent = { type: string; questionId?: number; payload?: string; sender?: string; timestamp?: number }

export type AiInterviewStatus = 'CREATED' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'
export type AiDifficulty = 'JUNIOR' | 'MID' | 'SENIOR' | 'STAFF'
export type AiInterviewMode = 'MIXED' | 'TECHNICAL_QA' | 'CODING' | 'SYSTEM_DESIGN' | 'BEHAVIORAL'
export type AiTurnStatus = 'ASKED' | 'EVALUATED' | 'SKIPPED'
export type AiEvaluationVerdict = 'EXCELLENT' | 'STRONG' | 'ADEQUATE' | 'NEEDS_IMPROVEMENT' | 'INSUFFICIENT'
export type AiMonitorSeverity = 'INFO' | 'WARNING' | 'ALERT'
export type AiMonitorStatus = 'OPTIMAL' | 'ATTENTION_NEEDED' | 'FLAGGED'
export type AiRecommendation = 'STRONG_HIRE' | 'HIRE' | 'BORDERLINE' | 'NEEDS_PRACTICE'

export type CreateAiInterviewRequest = {
  title?: string
  topic: string
  roleTitle?: string
  difficulty: AiDifficulty
  mode: AiInterviewMode
  questionCount: number
  durationMinutes: number
  focusAreas?: string
  enableFollowUps?: boolean
  strictMonitoring?: boolean
}

export type SubmitAiAnswerRequest = {
  turnId?: number
  answerText: string
  audioBase64?: string
  audioMimeType?: string
  codeSnippet?: string
  codeLanguage?: string
  executionSummary?: string
  responseTimeSeconds?: number
  skipped?: boolean
}

export type MonitorFrameRequest = {
  imageBase64: string
  mimeType?: string
  turnId?: number
  localObservation?: string
}

export type EyeMonitorResult = {
  facePresent: boolean
  multiplePeople: boolean
  lookingAtScreen: boolean
  gazeDirection: string
  cheatingSuspected: boolean
  alertReason?: string
  monitoring: AiMonitoringSummary
}

export type RecordMonitorEventRequest = {
  eventType: string
  severity?: AiMonitorSeverity
  detail?: string
  turnId?: number
  metadata?: string
}

export type AiAnswerEvaluation = {
  score: number
  verdict: AiEvaluationVerdict
  technicalAccuracyScore: number
  completenessScore: number
  clarityScore: number
  problemSolvingScore: number
  strengths: string[]
  improvements: string[]
  missingConcepts: string[]
  feedback: string
  referenceAnswer?: string
  followUpQuestion?: string
}

export type AiInterviewTurn = {
  id: number
  turnNumber: number
  followUp: boolean
  parentTurnId?: number
  category: string
  topicTag?: string
  difficulty: AiDifficulty
  questionText: string
  revealedHints: string[]
  totalHintsAvailable: number
  starterCode?: string
  codeLanguage?: string
  expectedConcepts: string[]
  userAnswer?: string
  userCode?: string
  codeLanguageUsed?: string
  executionSummary?: string
  responseTimeSeconds?: number
  status: AiTurnStatus
  evaluation?: AiAnswerEvaluation
  askedAt?: string
  answeredAt?: string
}

export type AiMonitorEvent = {
  id: number
  eventType: string
  severity: AiMonitorSeverity
  detail: string
  turnId?: number
  focusScoreImpact: number
  occurredAt: string
}

export type AiMonitoringSummary = {
  monitorStatus: AiMonitorStatus
  focusIntegrityScore: number
  tabSwitchCount: number
  windowBlurCount: number
  pasteEventCount: number
  idleWarningCount: number
  eyeOffScreenCount: number
  cheatingAlertCount: number
  questionsAnswered: number
  questionsSkipped: number
  totalPlannedQuestions: number
  elapsedSeconds: number
  averageResponseSeconds: number
  runningAverageScore: number
  scoreTrend: string
  liveObservation: string
  alerts: string[]
}

export type AiFinalReport = {
  overallScore: number
  recommendation: AiRecommendation
  executiveSummary: string
  technicalDepthScore: number
  problemSolvingScore: number
  communicationScore: number
  consistencyScore: number
  integrityFocusScore: number
  topStrengths: string[]
  improvementAreas: string[]
  studyPlan: string[]
  monitoringVerdict: string
  generatedAt?: string
}

export type AiInterviewSession = {
  id: number
  title: string
  topic: string
  roleTitle?: string
  difficulty: AiDifficulty
  mode: AiInterviewMode
  status: AiInterviewStatus
  questionCount: number
  durationMinutes: number
  focusAreas?: string
  enableFollowUps: boolean
  strictMonitoring: boolean
  candidateEmail: string
  currentTurnIndex: number
  overallScore?: number
  recommendation?: AiRecommendation
  aiProviderUsed?: string
  monitoring: AiMonitoringSummary
  finalReport?: AiFinalReport
  turns: AiInterviewTurn[]
  monitorEvents: AiMonitorEvent[]
  startedAt?: string
  completedAt?: string
  createdAt: string
}

export type AiInterviewSummary = {
  id: number
  title: string
  topic: string
  roleTitle?: string
  difficulty: AiDifficulty
  mode: AiInterviewMode
  status: AiInterviewStatus
  questionCount: number
  answeredCount: number
  durationMinutes: number
  overallScore?: number
  focusIntegrityScore?: number
  recommendation?: AiRecommendation
  createdAt: string
  completedAt?: string
}
