import { useMemo, useRef, useState } from 'react'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query'
import { FileUp, Pencil, Plus, Send, X } from 'lucide-react'
import { useAuth } from '../../app/auth'
import { workApi } from '../../api/resources'
import { ApiError } from '../../api/client'
import { EmptyState, PageError, PageLoading, Status, SuccessMessage } from '../../components/ui/Feedback'
import type { EvidenceMetadata, WorkEntry, WorkPeriod } from '../../types/domain'
import { formatDate, formatHours, titleCase } from '../../utils/format'

const entrySchema = z.object({
  activityTypeId: z.string().min(1, 'Choose an activity.'),
  workDate: z.string().min(10, 'Choose a work date.'),
  startTime: z.string().min(1, 'Enter a start time.'),
  endTime: z.string().min(1, 'Enter an end time.'),
  breakMinutes: z.number().int().min(0, 'Break must be zero or more minutes.'),
})
type EntryForm = z.infer<typeof entrySchema>

export function WorkPage() {
  const { user } = useAuth()
  const cache = useQueryClient()
  const periods = useQuery({ queryKey: ['work-periods'], queryFn: workApi.periods })
  const activities = useQuery({ queryKey: ['activity-types'], queryFn: workApi.activities })
  const [periodId, setPeriodId] = useState('')
  const period = useMemo(() => periods.data?.find(item => item.id === periodId) ?? periods.data?.[0], [periods.data, periodId])
  const entries = useQuery({
    queryKey: ['work-entries', user?.id, period?.id],
    queryFn: async () => {
      const list = await workApi.entries(user!.id, period!.id)
      return Promise.all(list.map(async entry => ({ ...entry, submission: await workApi.submissionForEntry(entry.id) })))
    },
    enabled: !!user && !!period,
  })
  const [editing, setEditing] = useState<WorkEntry | null>(null)
  const [message, setMessage] = useState('')

  const save = useMutation({
    mutationFn: (input: EntryForm) => editing
      ? workApi.updateEntry(editing.id, input)
      : workApi.createEntry(user!.id, period!.id, input),
    onSuccess: async () => {
      setEditing(null)
      setMessage(editing ? 'Draft updated.' : 'Work entry saved as a draft.')
      await Promise.all([
        cache.invalidateQueries({ queryKey: ['work-entries', user?.id, period?.id] }),
        cache.invalidateQueries({ queryKey: ['dashboard', user?.id, period?.id] }),
      ])
    },
  })

  const submission = useMutation({
    mutationFn: async (entryId: string) => {
      const existing = await workApi.submissionForEntry(entryId)
      const current = existing ?? await workApi.createSubmission(entryId)
      const next = current.status === 'CHANGES_REQUESTED' ? 'RESUBMITTED' : current.status === 'DRAFT' ? 'SUBMITTED' : null
      return next ? workApi.transitionSubmission(current.id, next) : current
    },
    onSuccess: async result => {
      setMessage(result.status === 'SUBMITTED' || result.status === 'RESUBMITTED' ? 'Submission sent for review.' : `This submission is already ${titleCase(result.status)}.`)
      await Promise.all([cache.invalidateQueries({ queryKey: ['work-entries', user?.id, period?.id] }), cache.invalidateQueries({ queryKey: ['dashboard', user?.id, period?.id] })])
    },
  })

  if (periods.isLoading || activities.isLoading) return <PageLoading label="Loading work tools" />
  if (periods.isError) return <PageError error={periods.error} />
  if (activities.isError) return <PageError error={activities.error} />

  return <div className="page-stack">
    <div className="page-heading"><div><span className="eyebrow">YOUR WORK</span><h1>Work entries</h1><p>Record time against an activity, then send completed drafts for review.</p></div>{period && <button className="button button-primary" onClick={() => { setEditing(null); setMessage(''); document.getElementById('work-entry-form')?.scrollIntoView({ behavior: 'smooth', block: 'start' }) }}><Plus size={17} /> New entry</button>}</div>
    {message && <SuccessMessage>{message}</SuccessMessage>}
    {periods.data?.length ? <section className="period-bar"><div><span className="eyebrow">WORK PERIOD</span><div><strong>{period?.name}</strong><span>{period && `${formatDate(period.startDate)} – ${formatDate(period.endDate)} · ${formatHours(period.weeklyHoursTarget)} weekly target`}</span></div></div><label className="compact-select"><span className="sr-only">Select work period</span><select value={period?.id ?? ''} onChange={event => { setPeriodId(event.target.value); setMessage('') }}>{periods.data.map((item: WorkPeriod) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label></section> : <EmptyState title="No work periods are configured">Ask an administrator to set up a work period before recording hours.</EmptyState>}

    {period && <section className="panel form-panel" id="work-entry-form"><div className="section-heading"><div><span className="eyebrow">{editing ? 'EDIT DRAFT' : 'NEW RECORD'}</span><h2>{editing ? 'Update work entry' : 'Record work'}</h2></div>{editing && <button className="icon-button" onClick={() => setEditing(null)} aria-label="Cancel editing"><X size={18} /></button>}</div>
      <EntryFormView key={editing?.id ?? 'new'} period={period} activities={activities.data ?? []} initial={editing} busy={save.isPending} error={save.error} onSubmit={values => { setMessage(''); save.mutate(values) }} />
    </section>}

    {entries.isLoading && <PageLoading label="Loading work entries" />}
    {entries.isError && <PageError error={entries.error} />}
    {entries.data && <section className="panel"><div className="section-heading"><div><span className="eyebrow">PERIOD RECORD</span><h2>Recorded work</h2></div><span className="muted">{entries.data.length} {entries.data.length === 1 ? 'entry' : 'entries'}</span></div>
      {entries.data.length === 0 ? <EmptyState title="Nothing recorded yet">Use the form above to add your first work entry for this period.</EmptyState> : <div className="table-wrap"><table><thead><tr><th>Date</th><th>Activity</th><th>Time</th><th>Duration</th><th>Work</th><th>Submission</th><th className="actions-col">Actions</th></tr></thead><tbody>{entries.data.map(entry => <tr key={entry.id}><td>{formatDate(entry.workDate)}</td><td>{activities.data?.find(activity => activity.id === entry.activityTypeId)?.name ?? 'Activity'}</td><td>{entry.startTime}–{entry.endTime}</td><td>{formatHours(entry.durationMinutes / 60)}</td><td><Status value={entry.status} /></td><td>{entry.submission ? <Status value={entry.submission.status} /> : <span className="muted">Not submitted</span>}</td><td><div className="row-actions">{entry.status === 'DRAFT' && <button className="button button-small button-secondary" onClick={() => { setEditing(entry); setMessage('') }}><Pencil size={14} /> Edit</button>}<EvidencePanel workEntryId={entry.id} /><SubmissionAction entry={entry} status={entry.submission?.status} pending={submission.isPending} onSubmit={() => submission.mutate(entry.id)} /></div></td></tr>)}</tbody></table></div>}
    </section>}
    {save.error && <PageError error={save.error} />}{submission.error && <PageError error={submission.error} />}
  </div>
}

function EntryFormView({ period, activities, initial, busy, error, onSubmit }: { period: WorkPeriod; activities: {id:string;name:string;active:boolean}[]; initial: WorkEntry | null; busy: boolean; error: unknown; onSubmit: (values: EntryForm) => void }) {
  const now = new Date()
  const defaultDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  const form = useForm<EntryForm>({ resolver: zodResolver(entrySchema), defaultValues: initial ? {
    activityTypeId: initial.activityTypeId, workDate: initial.workDate, startTime: initial.startTime,
    endTime: initial.endTime, breakMinutes: initial.breakMinutes,
  } : { activityTypeId: '', workDate: defaultDate < period.startDate ? period.startDate : defaultDate > period.endDate ? period.endDate : defaultDate, startTime: '', endTime: '', breakMinutes: 0 } })
  return <form className="form-grid" onSubmit={form.handleSubmit(onSubmit)} noValidate>
    <label className="field"><span>Activity</span><select {...form.register('activityTypeId')}><option value="">Choose an activity</option>{activities.filter(activity => activity.active).map(activity => <option key={activity.id} value={activity.id}>{activity.name}</option>)}</select><FieldError value={form.formState.errors.activityTypeId?.message} /></label>
    <label className="field"><span>Work date</span><input type="date" min={period.startDate} max={period.endDate} {...form.register('workDate')} /><FieldError value={form.formState.errors.workDate?.message} /></label>
    <label className="field"><span>Start time</span><input type="time" {...form.register('startTime')} /><FieldError value={form.formState.errors.startTime?.message} /></label>
    <label className="field"><span>End time</span><input type="time" {...form.register('endTime')} /><FieldError value={form.formState.errors.endTime?.message} /></label>
    <label className="field"><span>Break (minutes)</span><input type="number" min="0" step="1" {...form.register('breakMinutes', { valueAsNumber: true })} /><FieldError value={form.formState.errors.breakMinutes?.message} /></label>
    <div className="form-note">Effective duration is calculated and validated by Workhub. The current API records activity and time; it does not yet capture a written work description.</div>
    {error && <div className="form-full"><PageError error={error} /></div>}
    <div className="form-actions form-full"><button className="button button-primary" disabled={busy}>{busy ? 'Saving…' : initial ? 'Save changes' : 'Save draft'}</button></div>
  </form>
}

function FieldError({ value }: { value?: string }) { return value ? <small className="field-error">{value}</small> : null }

function SubmissionAction({ entry, status, pending, onSubmit }: { entry: WorkEntry; status?: string; pending: boolean; onSubmit: () => void }) {
  if (!entry.status || entry.status !== 'DRAFT') return null
  if (status && !['DRAFT', 'CHANGES_REQUESTED'].includes(status)) return null
  return <button className="button button-small button-quiet" disabled={pending} onClick={onSubmit}><Send size={14} /> {pending ? 'Sending…' : status === 'CHANGES_REQUESTED' ? 'Resubmit' : 'Submit'}</button>
}

function EvidencePanel({ workEntryId }: { workEntryId: string }) {
  const [open, setOpen] = useState(false)
  const [file, setFile] = useState<File | null>(null)
  const [purpose, setPurpose] = useState('Work evidence')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const pendingUpload = useRef<EvidenceMetadata | null>(null)
  const cache = useQueryClient()
  const metadata = useQuery({ queryKey: ['evidence', workEntryId], queryFn: () => workApi.evidence(workEntryId), enabled: open, retry: (count, cause) => !(cause instanceof ApiError && cause.status === 404) && count < 1 })
  const upload = useMutation({
    mutationFn: async () => {
      if (!file) throw new Error('Choose a PDF, JPEG or PNG file first.')
      if (file.size > 10 * 1024 * 1024) throw new Error('Evidence files must be 10 MB or smaller.')
      if (!['application/pdf', 'image/jpeg', 'image/png'].includes(file.type)) throw new Error('Choose a PDF, JPEG or PNG file.')
      const checksum = await sha256(file)
      const result = pendingUpload.current ?? await workApi.createEvidence(workEntryId, { mediaType: file.type, sizeBytes: file.size, checksum, purpose, changeNotes: notes })
      pendingUpload.current = result
      await workApi.uploadEvidenceBytes(workEntryId, result.privateObjectReferenceId, file, file.type)
      return result
    },
    onSuccess: async result => { pendingUpload.current = null; setSuccess(`Evidence version ${result.versionNumber} uploaded.`); setFile(null); await cache.invalidateQueries({ queryKey: ['evidence', workEntryId] }) },
    onError: cause => setError(cause instanceof Error ? cause.message : 'The evidence could not be uploaded.'),
  })
  return <div className="evidence-cell"><button className="button button-small button-secondary" onClick={() => { setOpen(value => !value); setError(''); setSuccess('') }}><FileUp size={14} /> Evidence</button>{open && <div className="inline-workspace"><div className="inline-heading"><strong>Evidence for this entry</strong><button className="icon-button" onClick={() => setOpen(false)} aria-label="Close evidence section"><X size={16} /></button></div>
    {metadata.data && <div className="evidence-summary"><span>Latest version: v{metadata.data.versionNumber} · {metadata.data.mediaType} · {(metadata.data.sizeBytes / 1024).toFixed(0)} KB</span><button className="text-link" onClick={async () => { try { const blob = await workApi.downloadEvidence(workEntryId, metadata.data!.privateObjectReferenceId); const url = URL.createObjectURL(blob); const anchor = document.createElement('a'); anchor.href = url; anchor.download = `evidence-v${metadata.data!.versionNumber}`; anchor.click(); URL.revokeObjectURL(url) } catch (cause) { setError(cause instanceof Error ? cause.message : 'Download failed') } }}>Download private file</button></div>}
    {metadata.isError && !(metadata.error instanceof ApiError && metadata.error.status === 404) && <PageError error={metadata.error} />}
    <label className="field"><span>File (PDF, JPEG or PNG · up to 10 MB)</span><input type="file" accept="application/pdf,image/jpeg,image/png" onChange={event => { pendingUpload.current = null; setFile(event.target.files?.[0] ?? null) }} /></label>
    <label className="field"><span>Purpose</span><input value={purpose} onChange={event => setPurpose(event.target.value)} required maxLength={120} /></label>
    <label className="field"><span>Change note <small>(optional)</small></span><input value={notes} onChange={event => setNotes(event.target.value)} maxLength={240} /></label>
    {error && <div className="alert alert-error" role="alert">{error}</div>}{success && <SuccessMessage>{success}</SuccessMessage>}
    <button className="button button-primary button-small" disabled={upload.isPending || !file} onClick={() => { setError(''); setSuccess(''); upload.mutate() }}>{upload.isPending ? 'Uploading…' : pendingUpload.current ? 'Retry file upload' : 'Upload new version'}</button>
    <small className="privacy-note">Files are sent to the authenticated Workhub service and stored through its configured private storage provider. File contents are not written to application logs.</small>
  </div>}</div>
}

async function sha256(file: Blob) {
  const digest = await crypto.subtle.digest('SHA-256', await file.arrayBuffer())
  return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')
}
