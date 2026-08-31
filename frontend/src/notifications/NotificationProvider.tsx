import { useEffect, useState, type ReactNode } from 'react'
import { CheckCircle2, Info, X, XCircle } from 'lucide-react'
import type { AppNotification } from './notificationBus'

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [notifications, setNotifications] = useState<Array<AppNotification & { id: number }>>([])
  useEffect(() => {
    const handleNotification = (event: Event) => {
      const notification = (event as CustomEvent<AppNotification>).detail
      const id = Date.now() + Math.random()
      setNotifications((current) => [...current, { ...notification, id }])
      window.setTimeout(() => setNotifications((current) => current.filter((item) => item.id !== id)), 5000)
    }
    window.addEventListener('codejit:notification', handleNotification)
    return () => window.removeEventListener('codejit:notification', handleNotification)
  }, [])
  const remove = (id: number) => setNotifications((current) => current.filter((item) => item.id !== id))
  return <><div className="pointer-events-none fixed right-4 top-4 z-50 flex w-[min(380px,calc(100vw-2rem))] flex-col gap-3">{notifications.map((item) => <Toast key={item.id} notification={item} onClose={() => remove(item.id)} />)}</div>{children}</>
}
function Toast({ notification, onClose }: { notification: AppNotification; onClose: () => void }) {
  const error = notification.kind === 'error'; const success = notification.kind === 'success'
  return <div role="status" className={`pointer-events-auto flex gap-3 border p-4 shadow-2xl backdrop-blur ${error ? 'border-[#743f3a] bg-[#241716]' : success ? 'border-[#405436] bg-[#182019]' : 'border-[#3e4f68] bg-[#151d2b]'}`}><div className={error ? 'text-[#f08c7d]' : success ? 'text-[#a7c982]' : 'text-[#88a8ff]'}>{error ? <XCircle size={19} /> : success ? <CheckCircle2 size={19} /> : <Info size={19} />}</div><div className="min-w-0 flex-1"><p className="text-sm font-extrabold text-white">{notification.title}</p><p className="mt-1 text-xs leading-5 text-[#b9bdc8]">{notification.message}</p></div><button onClick={onClose} aria-label="Dismiss notification" className="h-fit text-[#7f8492] hover:text-white"><X size={16} /></button></div>
}
