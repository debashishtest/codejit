import { useState } from 'react'
import type * as React from 'react'
import { ArrowRight, ScanLine } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'

export function JoinPage() {
  const [code, setCode] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const submit = async (event: React.FormEvent) => { event.preventDefault(); setError(''); setLoading(true); try { const assessment = await api.assessmentByCode(code.trim().toUpperCase()); await api.joinAssessment(code.trim().toUpperCase()); navigate(`/assessments/${assessment.id}/solve`) } catch (err) { setError(err instanceof Error ? err.message : 'Assessment not found') } finally { setLoading(false) } }
  return <div className="grid-noise min-h-[calc(100vh-76px)] px-5 py-12 sm:px-8 lg:px-10"><div className="mx-auto w-full max-w-[620px] slide-in"><p className="mono text-[10px] uppercase tracking-[.22em] text-[#f3c969]">Candidate access</p><h1 className="mt-4 text-4xl font-extrabold tracking-[-.07em] text-white">Enter the room.</h1><p className="mt-3 max-w-md text-sm leading-6 text-[#858a98]">Use the share code from your interviewer to join an active coding assessment.</p><form onSubmit={submit} className="mt-10 border border-white/[.08] bg-[#11141a] p-5 sm:p-8"><label className="block"><span className="mb-3 block text-xs font-bold uppercase tracking-[.12em] text-[#858a98]">Assessment code</span><div className="flex items-center gap-3 border border-white/[.12] bg-[#0d0f14] px-4 focus-within:border-[#f3c969]"><ScanLine size={19} className="text-[#f3c969]" /><input required minLength={4} maxLength={12} value={code} onChange={(event) => setCode(event.target.value)} placeholder="AB12CD34" className="mono w-full bg-transparent py-4 text-lg uppercase tracking-[.2em] text-white outline-none placeholder:text-[#4d515c]" /></div></label>{error && <p className="mt-4 border border-[#683d38] bg-[#241716] px-3 py-2 text-xs text-[#f08c7d]">{error}</p>}<button disabled={loading} className="mt-6 flex w-full items-center justify-center gap-2 bg-[#f3c969] px-4 py-3 text-sm font-extrabold text-[#17140d] hover:bg-[#ffe092] disabled:opacity-60">{loading ? 'Finding room...' : 'Continue to assessment'}{!loading && <ArrowRight size={16} />}</button></form></div></div>
}
