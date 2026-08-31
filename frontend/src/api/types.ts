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
