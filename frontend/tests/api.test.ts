import './setup.ts'
import { describe, it } from 'node:test'
import assert from 'node:assert'
import { api } from '../src/api/client.ts'

describe('API Client Unit Tests', () => {
  it('should export all required assessment and interview endpoints', () => {
    assert.strictEqual(typeof api.login, 'function')
    assert.strictEqual(typeof api.register, 'function')
    assert.strictEqual(typeof api.assessments, 'function')
    assert.strictEqual(typeof api.assessment, 'function')
    assert.strictEqual(typeof api.createAssessment, 'function')
    assert.strictEqual(typeof api.assessmentByCode, 'function')
    assert.strictEqual(typeof api.joinAssessment, 'function')
    assert.strictEqual(typeof api.startAssessment, 'function')
    assert.strictEqual(typeof api.run, 'function')
    assert.strictEqual(typeof api.submit, 'function')
    assert.strictEqual(typeof api.submission, 'function')
    assert.strictEqual(typeof api.interviews, 'function')
    assert.strictEqual(typeof api.interview, 'function')
    assert.strictEqual(typeof api.createInterview, 'function')
    assert.strictEqual(typeof api.interviewByCode, 'function')
    assert.strictEqual(typeof api.joinInterview, 'function')
    assert.strictEqual(typeof api.startInterview, 'function')
    assert.strictEqual(typeof api.endInterview, 'function')
    assert.strictEqual(typeof api.aiInterviews, 'function')
    assert.strictEqual(typeof api.aiInterview, 'function')
    assert.strictEqual(typeof api.createAiInterview, 'function')
    assert.strictEqual(typeof api.submitAiAnswer, 'function')
    assert.strictEqual(typeof api.requestAiHint, 'function')
    assert.strictEqual(typeof api.recordAiMonitorEvent, 'function')
    assert.strictEqual(typeof api.monitorAiFrame, 'function')
    assert.strictEqual(typeof api.completeAiInterview, 'function')
  })

  it('should call AI interview endpoints with proper paths and payloads', async () => {
    const calls: Array<{ url: string; method?: string; body?: string }> = []
    const originalFetch = globalThis.fetch
    globalThis.fetch = (async (url: string | URL | Request, init?: RequestInit) => {
      calls.push({ url: String(url), method: init?.method, body: init?.body ? String(init.body) : undefined })
      return new Response(JSON.stringify({ id: 1, status: 'IN_PROGRESS' }), { status: 200, headers: { 'Content-Type': 'application/json' } })
    }) as typeof fetch

    try {
      await api.createAiInterview({ topic: 'System Design', difficulty: 'SENIOR', mode: 'SYSTEM_DESIGN', questionCount: 4, durationMinutes: 30 })
      await api.submitAiAnswer(1, { turnId: 10, answerText: 'Token bucket with Redis Lua', audioBase64: 'dGVzdA==', audioMimeType: 'audio/webm', responseTimeSeconds: 42 })
      await api.recordAiMonitorEvent(1, { eventType: 'TAB_SWITCH', severity: 'WARNING', detail: 'Left tab' })
      await api.monitorAiFrame(1, { imageBase64: 'dGVzdA==', mimeType: 'image/jpeg', localObservation: 'GAZE_AWAY' })
      await api.completeAiInterview(1)

      assert.strictEqual(calls.length, 5)
      assert.strictEqual(calls[0].url, '/api/v1/ai-interviews')
      assert.strictEqual(calls[0].method, 'POST')
      assert.strictEqual(calls[1].url, '/api/v1/ai-interviews/1/answer')
      assert.strictEqual(calls[2].url, '/api/v1/ai-interviews/1/monitor-events')
      assert.strictEqual(calls[3].url, '/api/v1/ai-interviews/1/monitor-frame')
      assert.strictEqual(calls[4].url, '/api/v1/ai-interviews/1/complete')
    } finally {
      globalThis.fetch = originalFetch
    }
  })
})

