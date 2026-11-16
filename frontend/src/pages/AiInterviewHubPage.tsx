import { useEffect, useState, type FormEvent } from 'react'
import {
  ArrowRight,
  Bot,
  CheckCircle2,
  Clock,
  Cpu,
  Plus,
  ShieldCheck,
  Sparkles,
  TrendingUp
} from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import {
  api,
  type AiDifficulty,
  type AiInterviewMode,
  type AiInterviewSummary
} from '../api'
import { notify } from '../notifications/notificationBus'

type Preset = {
  label: string
  topic: string
  roleTitle: string
  difficulty: AiDifficulty
  mode: AiInterviewMode
  focusAreas: string
}

const PRESETS: Preset[] = [
  {
    label: 'Java & Spring Boot',
    topic: 'Java & Spring Boot',
    roleTitle: 'Backend Java Engineer',
    difficulty: 'MID',
    mode: 'MIXED',
    focusAreas: 'Concurrency, CompletableFuture, Spring AOP & Transactions',
  },
  {
    label: 'DSA & Algorithms',
    topic: 'Data Structures & Algorithms',
    roleTitle: 'Software Engineer',
    difficulty: 'MID',
    mode: 'CODING',
    focusAreas: 'Sliding Window, HashMap, LRU Cache, Graph Traversal',
  },
  {
    label: 'React & TypeScript',
    topic: 'Frontend React & TypeScript',
    roleTitle: 'Senior Frontend Engineer',
    difficulty: 'SENIOR',
    mode: 'MIXED',
    focusAreas: 'Reconciliation, Stale Closures, Async Race Conditions',
  },
  {
    label: 'System Design',
    topic: 'System Design & Distributed Systems',
    roleTitle: 'Staff Engineer',
    difficulty: 'SENIOR',
    mode: 'SYSTEM_DESIGN',
    focusAreas: 'Rate Limiting, Idempotency, Caching, Outbox Pattern',
  },
  {
    label: 'Python Backend',
    topic: 'Python Backend & APIs',
    roleTitle: 'Python Backend Engineer',
    difficulty: 'MID',
    mode: 'MIXED',
    focusAreas: 'GIL, Asyncio, Heap & Frequency Algorithms',
  },
  {
    label: 'Behavioral & Leadership',
    topic: 'Behavioral & Engineering Leadership',
    roleTitle: 'Senior Software Engineer',
    difficulty: 'SENIOR',
    mode: 'BEHAVIORAL',
    focusAreas: 'Incident Ownership, STAR Method, Technical Trade-offs',
  },
]

export function AiInterviewHubPage() {
  const [sessions, setSessions] = useState<AiInterviewSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    api.aiInterviews()
      .then((list) => {
        setSessions(list)
        if (list.length === 0) setShowForm(true)
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Unable to load AI practice interviews'))
      .finally(() => setLoading(false))
  }, [])

  const completed = sessions.filter((s) => s.status === 'COMPLETED' && s.overallScore !== undefined && s.overallScore !== null)
  const avgScore = completed.length > 0
    ? Math.round(completed.reduce((acc, s) => acc + (s.overallScore ?? 0), 0) / completed.length)
    : null
  const withFocus = sessions.filter((s) => s.focusIntegrityScore !== undefined && s.focusIntegrityScore !== null)
  const avgFocus = withFocus.length > 0
    ? Math.round(withFocus.reduce((acc, s) => acc + (s.focusIntegrityScore ?? 100), 0) / withFocus.length)
    : null

  return (
    <div className="grid-noise min-h-[calc(100vh-76px)] px-5 py-8 sm:px-8 lg:px-10 lg:py-12">
      <div className="mx-auto max-w-[1200px] slide-in">
        <header className="flex flex-col justify-between gap-6 sm:flex-row sm:items-end">
          <div>
            <p className="mono mb-3 text-[10px] uppercase tracking-[.24em] text-[#f3c969]">AI Interview Practice</p>
            <h1 className="text-3xl font-extrabold tracking-[-.06em] text-white sm:text-[42px]">
              Practice with an AI interviewer<span className="text-[#f3c969]">.</span>
            </h1>
            <p className="mt-2 max-w-2xl text-sm text-[#858a98]">
              Create a realistic oral mock interview. Speak your answers directly into the microphone while AI strictly grades your technical depth and continuously monitors your eyes &amp; camera for cheating.
            </p>
          </div>
          <button
            onClick={() => setShowForm(!showForm)}
            className="flex items-center justify-center gap-2 bg-[#f3c969] px-4 py-3 text-sm font-extrabold text-[#17140d] transition hover:bg-[#e0b757]"
          >
            <Plus size={17} /> {showForm ? 'Hide builder' : 'New AI interview'}
          </button>
        </header>

        {/* Summary Metrics */}
        <div className="mt-8 grid gap-px border border-white/[.08] bg-white/[.08] sm:grid-cols-3">
          <div className="bg-[#11141a] p-5">
            <div className="flex items-center justify-between text-[#7f8492]">
              <span className="text-xs font-bold uppercase tracking-[.1em]">Practice sessions</span>
              <Bot size={17} />
            </div>
            <div className="mt-4 flex items-end gap-3">
              <span className="text-3xl font-extrabold tracking-[-.06em] text-white">{String(sessions.length).padStart(2, '0')}</span>
              <span className="text-xs font-semibold text-[#89a96b]">{completed.length} completed</span>
            </div>
          </div>

          <div className="bg-[#11141a] p-5">
            <div className="flex items-center justify-between text-[#7f8492]">
              <span className="text-xs font-bold uppercase tracking-[.1em]">Avg interview score</span>
              <TrendingUp size={17} />
            </div>
            <div className="mt-4 flex items-end gap-3">
              <span className="text-3xl font-extrabold tracking-[-.06em] text-white">{avgScore !== null ? `${avgScore}%` : '—'}</span>
              <span className="text-xs font-semibold text-[#f3c969]">AI answer evaluation</span>
            </div>
          </div>

          <div className="bg-[#11141a] p-5">
            <div className="flex items-center justify-between text-[#7f8492]">
              <span className="text-xs font-bold uppercase tracking-[.1em]">Avg focus integrity</span>
              <ShieldCheck size={17} />
            </div>
            <div className="mt-4 flex items-end gap-3">
              <span className="text-3xl font-extrabold tracking-[-.06em] text-white">{avgFocus !== null ? `${avgFocus}%` : '—'}</span>
              <span className="text-xs font-semibold text-[#89a96b]">Complete session monitor</span>
            </div>
          </div>
        </div>

        {/* Builder Form */}
        {showForm && (
          <CreateAiInterviewForm
            onCreated={(sessionId) => {
              notify({ kind: 'success', title: 'AI Interview Ready', message: 'AI interviewer is ready with Question 1.' })
              navigate(`/ai-interviews/${sessionId}`)
            }}
          />
        )}

        {error && <p className="mt-6 border border-[#683d38] bg-[#241716] p-3 text-xs text-[#f08c7d]">{error}</p>}

        {/* Sessions List */}
        <section className="mt-10">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-extrabold text-white">Your AI Practice History</h2>
            <span className="mono text-[11px] text-[#7f8492]">{sessions.length} total</span>
          </div>

          {loading ? (
            <div className="mt-4 border border-white/[.08] bg-[#11141a] p-8 text-center text-xs text-[#858a98]">
              Loading AI practice interviews...
            </div>
          ) : sessions.length === 0 ? (
            <div className="mt-4 border border-dashed border-white/[.14] bg-[#11141a]/60 p-10 text-center text-sm text-[#858a98]">
              No AI practice sessions yet. Configure a topic above to begin your first AI-monitored practice interview.
            </div>
          ) : (
            <div className="mt-4 space-y-3">
              {sessions.map((session) => (
                <div
                  key={session.id}
                  className="flex flex-col justify-between gap-4 border border-white/[.08] bg-[#11141a] p-5 transition hover:border-[#f3c969]/60 sm:flex-row sm:items-center"
                >
                  <div className="flex items-start gap-4 min-w-0 flex-1">
                    <div className="grid h-11 w-11 shrink-0 place-items-center bg-[#2a271d] text-[#f3c969]">
                      <Bot size={20} />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2.5">
                        <h3 className="text-base font-extrabold text-white">{session.title}</h3>
                        <span className={`mono border px-2 py-0.5 text-[10px] font-bold ${session.status === 'COMPLETED' ? 'border-[#89a96b]/40 bg-[#89a96b]/10 text-[#89a96b]' : 'border-[#f3c969]/40 bg-[#f3c969]/10 text-[#f3c969]'}`}>
                          {session.status}
                        </span>
                        {session.recommendation && (
                          <span className="mono border border-white/[.15] px-2 py-0.5 text-[10px] text-[#d5d7df]">
                            {session.recommendation.replace('_', ' ')}
                          </span>
                        )}
                      </div>
                      <p className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-[#858a98]">
                        <span>{session.topic}</span>
                        <span>·</span>
                        <span className="mono text-[11px]">{session.difficulty}</span>
                        <span>·</span>
                        <span className="mono text-[11px]">{session.mode.replace('_', ' ')}</span>
                        <span>·</span>
                        <span>{session.answeredCount}/{session.questionCount} answered</span>
                        <span>·</span>
                        <span><Clock size={12} className="mr-1 inline" />{session.durationMinutes}m</span>
                      </p>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-3 shrink-0">
                    {session.overallScore !== undefined && session.overallScore !== null && (
                      <div className="border border-white/[.1] bg-[#0d0f14] px-3 py-1.5 text-center">
                        <span className="mono block text-[9px] text-[#7f8492]">SCORE</span>
                        <span className="mono text-sm font-bold text-[#f3c969]">{session.overallScore}%</span>
                      </div>
                    )}
                    {session.focusIntegrityScore !== undefined && session.focusIntegrityScore !== null && (
                      <div className="border border-white/[.1] bg-[#0d0f14] px-3 py-1.5 text-center">
                        <span className="mono block text-[9px] text-[#7f8492]">FOCUS</span>
                        <span className="mono text-sm font-bold text-[#89a96b]">{session.focusIntegrityScore}%</span>
                      </div>
                    )}
                    <Link
                      to={`/ai-interviews/${session.id}`}
                      className="flex items-center gap-1.5 bg-[#f3c969] px-4 py-2.5 text-xs font-extrabold text-[#17140d] transition hover:bg-[#e0b757]"
                    >
                      {session.status === 'COMPLETED' ? (
                        <>
                          <CheckCircle2 size={14} /> View Report
                        </>
                      ) : (
                        <>
                          Resume Interview <ArrowRight size={14} />
                        </>
                      )}
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

function CreateAiInterviewForm({ onCreated }: { onCreated: (sessionId: number) => void }) {
  const [title, setTitle] = useState('')
  const [topic, setTopic] = useState('Java & Spring Boot')
  const [roleTitle, setRoleTitle] = useState('Software Engineer')
  const [difficulty, setDifficulty] = useState<AiDifficulty>('MID')
  const [mode, setMode] = useState<AiInterviewMode>('MIXED')
  const [questionCount, setQuestionCount] = useState(5)
  const [durationMinutes, setDurationMinutes] = useState(30)
  const [focusAreas, setFocusAreas] = useState('')
  const [enableFollowUps, setEnableFollowUps] = useState(true)
  const [strictMonitoring, setStrictMonitoring] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const applyPreset = (preset: Preset) => {
    setTopic(preset.topic)
    setRoleTitle(preset.roleTitle)
    setDifficulty(preset.difficulty)
    setMode(preset.mode)
    setFocusAreas(preset.focusAreas)
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const created = await api.createAiInterview({
        title: title.trim() || undefined,
        topic: topic.trim(),
        roleTitle: roleTitle.trim() || undefined,
        difficulty,
        mode,
        questionCount,
        durationMinutes,
        focusAreas: focusAreas.trim() || undefined,
        enableFollowUps,
        strictMonitoring,
      })
      onCreated(created.id)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create AI interview')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={submit} className="mt-8 border border-[#3b3523] bg-[#161714] p-5 sm:p-7">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Sparkles size={19} className="text-[#f3c969]" />
          <h2 className="text-base font-extrabold text-white">Configure AI Practice Interview</h2>
        </div>
        <span className="mono text-[10px] text-[#89a96b]">VOICE-ONLY ANSWERS · EYE &amp; CHEATING MONITOR · STRICT GRADING</span>
      </div>

      {/* Quick Presets */}
      <div className="mt-5">
        <span className="mono text-[10px] uppercase tracking-[.15em] text-[#858a98]">Quick templates</span>
        <div className="mt-2 flex flex-wrap gap-2">
          {PRESETS.map((preset) => (
            <button
              type="button"
              key={preset.label}
              onClick={() => applyPreset(preset)}
              className={`border px-3 py-1.5 text-xs font-bold transition ${topic === preset.topic ? 'border-[#f3c969] bg-[#f3c969]/15 text-[#f3c969]' : 'border-white/[.12] bg-[#11141a] text-[#c8cbd3] hover:border-[#f3c969]/50'}`}
            >
              {preset.label}
            </button>
          ))}
        </div>
      </div>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <label className="text-xs text-[#858a98]">
          Topic / Tech Stack *
          <input
            required
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            placeholder="e.g. Java & Spring Boot, System Design, React"
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>

        <label className="text-xs text-[#858a98]">
          Target Role
          <input
            value={roleTitle}
            onChange={(e) => setRoleTitle(e.target.value)}
            placeholder="e.g. Senior Backend Engineer"
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>

        <label className="text-xs text-[#858a98]">
          Difficulty Level
          <select
            value={difficulty}
            onChange={(e) => setDifficulty(e.target.value as AiDifficulty)}
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          >
            <option value="JUNIOR">Junior (Fundamentals & Core Patterns)</option>
            <option value="MID">Mid-Level (Production Engineering)</option>
            <option value="SENIOR">Senior (Architecture & Deep Trade-offs)</option>
            <option value="STAFF">Staff (Scale, Reliability & Strategy)</option>
          </select>
        </label>

        <label className="text-xs text-[#858a98]">
          Interview Format
          <select
            value={mode}
            onChange={(e) => setMode(e.target.value as AiInterviewMode)}
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          >
            <option value="MIXED">Mixed (Oral Q&amp;A + Verbal Coding + Design + Behavioral)</option>
            <option value="TECHNICAL_QA">Technical Q&amp;A Deep-Dive</option>
            <option value="CODING">Verbal Algorithm &amp; Code Walkthrough</option>
            <option value="SYSTEM_DESIGN">System Design &amp; Architecture</option>
            <option value="BEHAVIORAL">Behavioral (STAR &amp; Leadership)</option>
          </select>
        </label>

        <label className="text-xs text-[#858a98]">
          Number of Questions (1–10)
          <input
            type="number"
            min={1}
            max={10}
            value={questionCount}
            onChange={(e) => setQuestionCount(Math.max(1, Math.min(10, Number(e.target.value) || 5)))}
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>

        <label className="text-xs text-[#858a98]">
          Target Duration (Minutes)
          <input
            type="number"
            min={5}
            max={120}
            value={durationMinutes}
            onChange={(e) => setDurationMinutes(Math.max(5, Math.min(120, Number(e.target.value) || 30)))}
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>

        <label className="text-xs text-[#858a98]">
          Session Title (Optional)
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Auto-generated from topic & difficulty if left blank"
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>

        <label className="text-xs text-[#858a98]">
          Specific Focus Areas (Optional)
          <input
            value={focusAreas}
            onChange={(e) => setFocusAreas(e.target.value)}
            placeholder="e.g. Caching, Concurrency, Edge Cases"
            className="mt-2 w-full border border-white/[.12] bg-[#11141a] px-3 py-2.5 text-sm text-white outline-none focus:border-[#f3c969]"
          />
        </label>
      </div>

      <div className="mt-5 flex flex-wrap gap-6 border-t border-white/[.08] pt-4 text-xs text-[#c8cbd3]">
        <label className="flex cursor-pointer items-center gap-2.5">
          <input
            type="checkbox"
            checked={enableFollowUps}
            onChange={(e) => setEnableFollowUps(e.target.checked)}
            className="accent-[#f3c969]"
          />
          <span>Enable adaptive AI follow-up probing questions</span>
        </label>

        <label className="flex cursor-pointer items-center gap-2.5">
          <input
            type="checkbox"
            checked={strictMonitoring}
            onChange={(e) => setStrictMonitoring(e.target.checked)}
            className="accent-[#f3c969]"
          />
          <span>Strict AI proctoring (continuous eye tracking, camera presence &amp; tab-switch cheating alerts)</span>
        </label>
      </div>

      {error && <p className="mt-4 text-xs text-[#f08c7d]">{error}</p>}

      <button
        disabled={loading}
        className="mt-6 flex items-center gap-2 bg-[#f3c969] px-5 py-3 text-xs font-extrabold text-[#17140d] transition hover:bg-[#e0b757] disabled:opacity-50"
      >
        <Cpu size={16} /> {loading ? 'Preparing AI Interviewer...' : 'Start AI Practice Interview'}
      </button>
    </form>
  )
}
