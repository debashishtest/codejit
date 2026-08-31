import { describe, it } from 'node:test'
import assert from 'node:assert'
import type { Assessment, Interview, TestResult } from '../src/api/types.ts'

describe('Domain Models and Type Contracts', () => {
  it('should construct valid Assessment data structure', () => {
    const assessment: Assessment = {
      id: 1,
      title: 'Distributed Systems Assessment',
      shareCode: 'DIST1234',
      durationMinutes: 45,
      startTime: '2026-08-31T12:00:00',
      endTime: '2026-08-31T12:45:00',
      status: 'STARTED',
      questionCount: 2,
      questions: [
        {
          id: 10,
          question: 'Implement LRU Cache',
          questionNumber: 1,
          language: 'java',
          starterCode: 'class LRUCache {}',
          visibleTestCases: [
            { sequence: 0, input: 'put(1, 1)', expectedOutput: 'null' }
          ]
        }
      ]
    }

    assert.strictEqual(assessment.id, 1)
    assert.strictEqual(assessment.shareCode, 'DIST1234')
    assert.strictEqual(assessment.questions?.length, 1)
  })

  it('should construct valid Interview data structure', () => {
    const interview: Interview = {
      id: 5,
      title: 'Frontend Lead Screen',
      shareCode: 'LEAD5555',
      scheduledStart: '2026-08-31T15:00:00',
      scheduledEnd: '2026-08-31T16:00:00',
      status: 'LIVE',
      participants: [
        { userId: 1, username: 'interviewer@codejit.io', role: 'INTERVIEWER', online: true },
        { userId: 2, username: 'candidate@codejit.io', role: 'CANDIDATE', online: true }
      ]
    }

    assert.strictEqual(interview.status, 'LIVE')
    assert.strictEqual(interview.participants.length, 2)
  })

  it('should construct valid TestResult payload', () => {
    const result: TestResult = {
      sequence: 0,
      passed: true,
      actualOutput: '42',
      expectedOutput: '42',
      runtimeMillis: 34
    }

    assert.strictEqual(result.passed, true)
    assert.strictEqual(result.runtimeMillis, 34)
  })
})

