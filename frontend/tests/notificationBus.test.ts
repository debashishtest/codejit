import './setup.ts'
import { describe, it } from 'node:test'
import assert from 'node:assert'
import { notify, type AppNotification } from '../src/notifications/notificationBus.ts'

describe('Notification Bus Unit Tests', () => {
  it('should dispatch custom window event when notify is called', () => {
    let capturedEvent: AppNotification | null = null

    const handler = (event: any) => {
      capturedEvent = event.detail
    }

    window.addEventListener('codejit:notification', handler)

    notify({
      kind: 'success',
      title: 'Assessment Created',
      message: 'Assessment room has been configured successfully.'
    })

    window.removeEventListener('codejit:notification', handler)

    assert.ok(capturedEvent !== null, 'Event should be captured')
    assert.strictEqual(capturedEvent.kind, 'success')
    assert.strictEqual(capturedEvent.title, 'Assessment Created')
  })
})

