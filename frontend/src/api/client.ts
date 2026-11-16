import type {
  AiInterviewSession,
  AiInterviewSummary,
  AiMonitoringSummary,
  Assessment,
  AssessmentRequest,
  AssessmentSummary,
  AuthResponse,
  CodeRequest,
  CreateAiInterviewRequest,
  EyeMonitorResult,
  Interview,
  InterviewRequest,
  MonitorFrameRequest,
  RecordMonitorEventRequest,
  RunResponse,
  SubmissionResponse,
  SubmitAiAnswerRequest
} from './types.ts'
import { notify } from '../notifications/notificationBus.ts'

const API_BASE = (typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_URL) ? import.meta.env.VITE_API_URL : '/api/v1'

function extractErrorMessage(rawMessage: string): string | null {
  try {
    const parsed = JSON.parse(rawMessage) as { message?: string; error?: string }
    if (parsed && typeof parsed.message === 'string' && parsed.message.trim().length > 0) {
      return parsed.message
    }
    if (parsed && typeof parsed.error === 'string' && parsed.error.trim().length > 0 && parsed.error !== 'Internal Server Error') {
      return parsed.error
    }
  } catch {
    // Ignore non-JSON error bodies
  }
  return null
}

async function request<T>(path: string, options: RequestInit & { silent?: boolean } = {}): Promise<T> {
  const { silent = false, ...fetchOptions } = options
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('forge_token') : null
  const isPublicRequest = path.startsWith('/public/')
  const response = await fetch(`${API_BASE}${path}`, {
    ...fetchOptions,
    headers: { 'Content-Type': 'application/json', ...(!isPublicRequest && token ? { Authorization: `Bearer ${token}` } : {}), ...fetchOptions.headers },
  })
  if ((response.status === 401 || response.status === 403) && !isPublicRequest && typeof localStorage !== 'undefined') {
    localStorage.removeItem('forge_token')
    localStorage.removeItem('forge_username')
  }
  if (!response.ok) {
    const rawMessage = await response.text()
    const parsedMessage = extractErrorMessage(rawMessage)
    const message = response.status === 401
      ? 'Your session has expired. Please sign in again.'
      : response.status === 403
        ? 'You do not have permission to perform that action.'
        : response.status === 409
          ? 'That record already exists.'
          : response.status === 413
            ? 'Spoken audio recording was too large. Try submitting a shorter answer.'
            : (response.status === 502 || response.status === 503)
              ? 'Backend service is starting up or temporarily unavailable. Please wait a few seconds and try again.'
              : response.status === 504
                ? 'The AI request timed out. Please try again.'
                : rawMessage.includes('Email already exists')
                  ? 'That email is already registered. Try signing in instead.'
                  : parsedMessage ?? `Request failed (HTTP ${response.status}). Please try again.`
    if (!silent) {
      notify({ kind: 'error', title: response.status === 403 ? 'Permission denied' : 'Request not completed', message })
    }
    throw new Error(message)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  login: (username: string, password: string) => request<AuthResponse>('/public/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  register: (name: string, email: string, password: string) => request<AuthResponse>('/public/register', { method: 'POST', body: JSON.stringify({ name, email, password }) }),
  assessments: () => request<AssessmentSummary[]>('/assessments'),
  assessment: (id: number) => request<Assessment>(`/assessments/${id}`),
  createAssessment: (payload: AssessmentRequest) => request<Assessment>('/assessments', { method: 'POST', body: JSON.stringify(payload) }),
  assessmentByCode: (code: string) => request<Assessment>(`/assessments/join/${encodeURIComponent(code)}`),
  joinAssessment: (code: string) => request<void>(`/assessments/join/${encodeURIComponent(code)}`, { method: 'POST' }),
  startAssessment: (id: number) => request<void>(`/assessments/${id}/start`, { method: 'POST' }),
  run: (assessmentId: number, questionId: number, payload: CodeRequest) => request<RunResponse>(`/assessments/${assessmentId}/questions/${questionId}/run`, { method: 'POST', body: JSON.stringify(payload) }),
  executeCode: (payload: CodeRequest) => request<RunResponse>('/execution/run', { method: 'POST', body: JSON.stringify(payload) }),
  submit: (assessmentId: number, questionId: number, payload: CodeRequest) => request<SubmissionResponse>(`/assessments/${assessmentId}/questions/${questionId}/submit`, { method: 'POST', body: JSON.stringify(payload) }),
  submission: (id: number) => request<SubmissionResponse>(`/submissions/${id}`),
  interviews: () => request<Interview[]>('/interviews'),
  interview: (id: number) => request<Interview>(`/interviews/${id}`),
  createInterview: (payload: InterviewRequest) => request<Interview>('/interviews', { method: 'POST', body: JSON.stringify(payload) }),
  interviewByCode: (code: string) => request<Interview>(`/interviews/join/${encodeURIComponent(code)}`),
  joinInterview: (code: string) => request<Interview>(`/interviews/join/${encodeURIComponent(code)}`, { method: 'POST' }),
  startInterview: (id: number) => request<Interview>(`/interviews/${id}/start`, { method: 'POST' }),
  endInterview: (id: number) => request<Interview>(`/interviews/${id}/end`, { method: 'POST' }),
  aiInterviews: () => request<AiInterviewSummary[]>('/ai-interviews'),
  aiInterview: (id: number) => request<AiInterviewSession>(`/ai-interviews/${id}`),
  createAiInterview: (payload: CreateAiInterviewRequest) => request<AiInterviewSession>('/ai-interviews', { method: 'POST', body: JSON.stringify(payload) }),
  submitAiAnswer: (id: number, payload: SubmitAiAnswerRequest) => request<AiInterviewSession>(`/ai-interviews/${id}/answer`, { method: 'POST', body: JSON.stringify(payload) }),
  requestAiHint: (id: number, turnId?: number) => request<AiInterviewSession>(`/ai-interviews/${id}/hint`, { method: 'POST', body: JSON.stringify({ turnId }) }),
  recordAiMonitorEvent: (id: number, payload: RecordMonitorEventRequest) => request<AiMonitoringSummary>(`/ai-interviews/${id}/monitor-events`, { method: 'POST', body: JSON.stringify(payload), silent: true }),
  monitorAiFrame: (id: number, payload: MonitorFrameRequest) => request<EyeMonitorResult>(`/ai-interviews/${id}/monitor-frame`, { method: 'POST', body: JSON.stringify(payload), silent: true }),
  completeAiInterview: (id: number) => request<AiInterviewSession>(`/ai-interviews/${id}/complete`, { method: 'POST' }),
}
