// Setup test environment for Node test runner
class MockEventTarget {
  private listeners: Record<string, Function[]> = {}

  addEventListener(type: string, listener: Function) {
    if (!this.listeners[type]) this.listeners[type] = []
    this.listeners[type].push(listener)
  }

  removeEventListener(type: string, listener: Function) {
    if (this.listeners[type]) {
      this.listeners[type] = this.listeners[type].filter(l => l !== listener)
    }
  }

  dispatchEvent(event: any) {
    if (this.listeners[event.type]) {
      this.listeners[event.type].forEach(listener => listener(event))
    }
    return true
  }
}

class MockCustomEvent {
  type: string
  detail: any

  constructor(type: string, init?: { detail?: any }) {
    this.type = type
    this.detail = init?.detail
  }
}

if (typeof globalThis.window === 'undefined') {
  const target = new MockEventTarget()
  ;(globalThis as any).window = target
  ;(globalThis as any).CustomEvent = MockCustomEvent
  ;(globalThis as any).localStorage = {
    store: {} as Record<string, string>,
    getItem(key: string) { return this.store[key] ?? null },
    setItem(key: string, value: string) { this.store[key] = value },
    removeItem(key: string) { delete this.store[key] },
    clear() { this.store = {} }
  }
}

