import { describe, it } from 'node:test'
import assert from 'node:assert'
import type { AiInterviewSession, Assessment, Interview, TestResult } from '../src/api/types.ts'

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

  it('should construct valid AiInterviewSession with evaluation and monitoring state', () => {
    const aiSession: AiInterviewSession = {
      id: 9,
      title: 'System Design (SENIOR AI Practice)',
      topic: 'System Design',
      roleTitle: 'Staff Engineer',
      difficulty: 'SENIOR',
      mode: 'SYSTEM_DESIGN',
      status: 'COMPLETED',
      questionCount: 3,
      durationMinutes: 30,
      enableFollowUps: true,
      strictMonitoring: true,
      candidateEmail: 'dev@codejit.io',
      currentTurnIndex: 2,
      overallScore: 84,
      recommendation: 'STRONG_HIRE',
      aiProviderUsed: 'CODEJIT_AI_ENGINE',
      monitoring: {
        monitorStatus: 'OPTIMAL',
        focusIntegrityScore: 95,
        tabSwitchCount: 1,
        windowBlurCount: 0,
        pasteEventCount: 0,
        idleWarningCount: 0,
        eyeOffScreenCount: 0,
        cheatingAlertCount: 1,
        questionsAnswered: 3,
        questionsSkipped: 0,
        totalPlannedQuestions: 3,
        elapsedSeconds: 840,
        averageResponseSeconds: 90,
        runningAverageScore: 84,
        scoreTrend: 'IMPROVING',
        liveObservation: 'Consistent strong depth',
        alerts: ['1 tab switch(es) recorded during session']
      },
      turns: [
        {
          id: 101,
          turnNumber: 1,
          followUp: false,
          category: 'SYSTEM_DESIGN',
          topicTag: 'Rate Limiter',
          difficulty: 'SENIOR',
          questionText: 'Design a distributed rate limiter',
          revealedHints: [],
          totalHintsAvailable: 1,
          expectedConcepts: ['rate limiting', 'cache'],
          userAnswer: 'Use Redis Lua token bucket.',
          status: 'EVALUATED',
          evaluation: {
            score: 84,
            verdict: 'STRONG',
            technicalAccuracyScore: 85,
            completenessScore: 80,
            clarityScore: 88,
            problemSolvingScore: 83,
            strengths: ['Covered atomic Lua execution'],
            improvements: ['Discuss hot key sharding'],
            missingConcepts: [],
            feedback: 'Strong answer.'
          }
        }
      ],
      monitorEvents: [],
      createdAt: '2026-11-11T10:00:00'
    }

    assert.strictEqual(aiSession.status, 'COMPLETED')
    assert.strictEqual(aiSession.monitoring.focusIntegrityScore, 95)
    assert.strictEqual(aiSession.turns[0].evaluation?.verdict, 'STRONG')
  })
})

