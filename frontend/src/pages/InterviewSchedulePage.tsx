import { useEffect, useState, type FormEvent } from 'react'
import { ArrowRight, CalendarPlus, Copy, Plus, Radio, Users } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { api, type Interview } from '../api'
import { notify } from '../notifications/notificationBus'

export function InterviewSchedulePage() {
  const [interviews, setInterviews] = useState<Interview[]>([])
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    api.interviews()
      .then(setInterviews)
      .catch((err) => setError(err instanceof Error ? err.message : 'Unable to load interviews'))
  }, [])

  return (
    <div className="grid-noise min-h-[calc(100vh-76px)] px-5 py-8 sm:px-8 lg:px-10 lg:py-12">
      <div className="mx-auto max-w-[1100px] slide-in">
        <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
          <div>
            <p className="mono text-[10px] uppercase tracking-[.22em] text-[#f3c969]">Interview rooms</p>
            <h1 className="mt-3 text-3xl font-extrabold tracking-[-.06em] text-white">Schedule a live screen.</h1>
            <p className="mt-2 text-sm text-[#858a98]">Create a private room for real-time conversation, collaborative code, and judging.</p>
          </div>
          <button
            onClick={() => setShowForm(!showForm)}
            className="flex items-center justify-center gap-2 bg-[#f3c969] px-4 py-3 text-sm font-extrabold text-[#17140d] transition hover:bg-[#e0b757]"
          >
            <Plus size={17} /> Schedule interview
          </button>
        </div>

        {showForm && (
          <ScheduleForm
            onCreated={(interview) => {
              setInterviews([interview, ...interviews])
              setShowForm(false)
              notify({ kind: 'success', title: 'Interview Scheduled', message: 'Room created. Joining as Interviewer...' })
              navigate(`/interviews/${interview.id}`)
            }}
          />
        )}

        {error && <p className="mt-6 border border-[#683d38] bg-[#241716] p-3 text-xs text-[#f08c7d]">{error}</p>}

        <div className="mt-10 flex items-center justify-between">
          <h2 className="text-xl font-extrabold text-white">Your Interview Rooms</h2>
          <Link to="/interviews/join" className="text-xs font-bold text-[#f3c969] hover:underline">
            Join with a share code &rarr;
          </Link>
        </div>

        <div className="mt-4 space-y-3">
          {interviews.map((interview) => (
            <div
              key={interview.id}
              className="group flex flex-col gap-4 border border-white/[.08] bg-[#11141a] p-5 transition hover:border-[#f3c969]/60 sm:flex-row sm:items-center justify-between"
            >
              <div className="flex items-center gap-4 min-w-0 flex-1">
                <div className="grid h-11 w-11 shrink-0 place-items-center bg-[#2a271d] text-[#f3c969]">
                  <Radio size={19} />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-3">
                    <h3 className="font-extrabold text-white text-base">{interview.title}</h3>
                    <span className="mono border border-white/[.15] px-2 py-0.5 text-[10px] text-[#f3c969]">
                      {interview.status}
                    </span>
                  </div>
                  <p className="mt-1.5 text-xs text-[#858a98]">
                    {new Date(interview.scheduledStart).toLocaleString()}
                    <span className="mx-2 text-[#4d515c]">·</span>
                    <span className="inline-flex items-center gap-1">
                      <Users size={12} />
                      {interview.participants?.length ?? 1} participant{interview.participants?.length === 1 ? '' : 's'}
                    </span>
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-3 shrink-0">
                <div className="flex items-center gap-2 border border-[#665a36] bg-[#242015] px-3 py-2">
                  <span className="mono text-xs tracking-[.16em] text-[#f3c969] font-bold">{interview.shareCode}</span>
                  <button
                    onClick={(event) => {
                      event.preventDefault()
                      navigator.clipboard?.writeText(interview.shareCode)
                      notify({ kind: 'info', title: 'Code Copied', message: `Share code ${interview.shareCode} copied.` })
                    }}
                    aria-label="Copy interview code"
                    className="text-[#c6b776] hover:text-white"
                  >
                    <Copy size={14} />
                  </button>
                </div>

                <Link
                  to={`/interviews/${interview.id}`}
                  className="flex items-center gap-1.5 bg-[#f3c969] px-4 py-2 text-xs font-extrabold text-[#17140d] transition hover:bg-[#e0b757]"
                >
                  Join Room <ArrowRight size={13} />
                </Link>
              </div>
            </div>
          ))}

          {interviews.length === 0 && !error && (
            <div className="border border-dashed border-white/[.14] p-10 text-center text-sm text-[#858a98]">
              No interview rooms created yet. Click &quot;Schedule interview&quot; to set up your first live session.
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function ScheduleForm({ onCreated }: { onCreated: (interview: Interview) => void }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [start, setStart] = useState('')
  const [end, setEnd] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const now = new Date()
      const startIso = start ? new Date(start).toISOString().slice(0, 19) : now.toISOString().slice(0, 19)
      const endIso = end ? new Date(end).toISOString().slice(0, 19) : new Date(now.getTime() + 3600000).toISOString().slice(0, 19)

      const result = await api.createInterview({
        title,
        description,
        scheduledStart: startIso,
        scheduledEnd: endIso,
      })
      onCreated(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to schedule interview')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={submit} className="mt-8 border border-[#3b3523] bg-[#181710] p-5 sm:p-6">
      <div className="flex items-center gap-3 text-[#f3c969]">
        <CalendarPlus size={19} />
        <h2 className="font-extrabold text-white">New Live Interview Room</h2>
      </div>

      <div className="mt-5 grid gap-4 sm:grid-cols-2">
        <input
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Interview Title (e.g. Senior Frontend Technical Screen)"
          className="border border-white/[.12] bg-[#11141a] px-3 py-3 text-sm text-white outline-none focus:border-[#f3c969] sm:col-span-2"
        />
        <input
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Description or focus area (optional)"
          className="border border-white/[.12] bg-[#11141a] px-3 py-3 text-sm text-white outline-none focus:border-[#f3c969] sm:col-span-2"
        />
        <label className="text-xs text-[#858a98]">
          Starts
          <input
            type="datetime-local"
            value={start}
            onChange={(e) => setStart(e.target.value)}
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-3 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>
        <label className="text-xs text-[#858a98]">
          Ends
          <input
            type="datetime-local"
            value={end}
            onChange={(e) => setEnd(e.target.value)}
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-3 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>
      </div>

      {error && <p className="mt-4 text-xs text-[#f08c7d]">{error}</p>}

      <button
        disabled={loading}
        className="mt-5 flex items-center gap-2 bg-[#f3c969] px-5 py-3 text-xs font-extrabold text-[#17140d] transition hover:bg-[#e0b757] disabled:opacity-50"
      >
        <CalendarPlus size={15} /> {loading ? 'Creating...' : 'Create & Enter Room'}
      </button>
    </form>
  )
}
