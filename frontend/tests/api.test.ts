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
  })
})

