export type NotificationKind = 'success' | 'error' | 'info'
export type AppNotification = { kind: NotificationKind; title: string; message: string }

export function notify(notification: AppNotification) {
  window.dispatchEvent(new CustomEvent<AppNotification>('codejit:notification', { detail: notification }))
}
