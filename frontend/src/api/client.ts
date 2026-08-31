import type { Assessment, AssessmentRequest, AssessmentSummary, AuthResponse, CodeRequest, Interview, InterviewRequest, RunResponse, SubmissionResponse } from './types.ts'
import { notify } from '../notifications/notificationBus.ts'

const API_BASE = (typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_URL) ? import.meta.env.VITE_API_URL : '/api/v1'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('forge_token') : null
  const isPublicRequest = path.startsWith('/public/')
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(!isPublicRequest && token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
  })
  if ((response.status === 401 || response.status === 403) && !isPublicRequest && typeof localStorage !== 'undefined') {
    localStorage.removeItem('forge_token')
    localStorage.removeItem('forge_username')
  }
  if (!response.ok) {
    const rawMessage = await response.text()
    const message = response.status === 401
      ? 'Your session has expired. Please sign in again.'
      : response.status === 403
        ? 'You do not have permission to perform that action.'
        : response.status === 409
          ? 'That record already exists.'
          : rawMessage.includes('Email already exists')
            ? 'That email is already registered. Try signing in instead.'
            : 'Something went wrong. Please try again.'
    notify({ kind: 'error', title: response.status === 403 ? 'Permission denied' : 'Request not completed', message })
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
  submit: (assessmentId: number, questionId: number, payload: CodeRequest) => request<SubmissionResponse>(`/assessments/${assessmentId}/questions/${questionId}/submit`, { method: 'POST', body: JSON.stringify(payload) }),
  submission: (id: number) => request<SubmissionResponse>(`/submissions/${id}`),
  interviews: () => request<Interview[]>('/interviews'),
  interview: (id: number) => request<Interview>(`/interviews/${id}`),
  createInterview: (payload: InterviewRequest) => request<Interview>('/interviews', { method: 'POST', body: JSON.stringify(payload) }),
  interviewByCode: (code: string) => request<Interview>(`/interviews/join/${encodeURIComponent(code)}`),
  joinInterview: (code: string) => request<Interview>(`/interviews/join/${encodeURIComponent(code)}`, { method: 'POST' }),
  startInterview: (id: number) => request<Interview>(`/interviews/${id}/start`, { method: 'POST' }),
  endInterview: (id: number) => request<Interview>(`/interviews/${id}/end`, { method: 'POST' }),
}
