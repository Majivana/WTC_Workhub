import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Check, Download, Eye, MessageSquareText } from 'lucide-react'
import { useAuth } from '../../app/auth'
import { reviewApi, workApi } from '../../api/resources'
import { api } from '../../api/client'
import { EmptyState, PageError, PageLoading, Status, SuccessMessage } from '../../components/ui/Feedback'
import { formatDate, formatHours, formatTimestamp, titleCase } from '../../utils/format'
import type { QueueItem } from '../../types/domain'

export function ReviewPage() {
  const { user } = useAuth()
  const cache = useQueryClient()
  const [params, setParams] = useSearchParams()
  const queue = useQuery({ queryKey: ['review-queue', user?.id], queryFn: () => reviewApi.queue(user!.id), enabled: !!user })
  const activities = useQuery({ queryKey: ['activity-types'], queryFn: workApi.activities })
  const selectedId = params.get('submission') ?? ''
  const selected = queue.data?.pendingReviews.find(item => item.id === selectedId) ?? queue.data?.pendingReviews[0]
  const workEntry = useQuery({ queryKey: ['work-entry', selected?.work_entry_id], queryFn: () => workApi.entry(selected!.work_entry_id), enabled: !!selected })
  const [action, setAction] = useState<'APPROVE' | 'REQUEST_CHANGES' | 'REJECT'>('APPROVE')
  const [comment, setComment] = useState('')
  const [notice, setNotice] = useState('')
  const [localError, setLocalError] = useState('')
  const evidence = useQuery({ queryKey: ['evidence', selected?.work_entry_id], queryFn: () => workApi.evidence(selected!.work_entry_id), enabled: !!selected, retry: (count, error) => !(error instanceof Error && 'status' in error && error.status === 404) && count < 1 })
  const history = useQuery({ queryKey: ['verification-history', selected?.id], queryFn: () => reviewApi.verificationHistory(selected!.id), enabled: !!selected })
  const [started, setStarted] = useState(false)
  useEffect(() => { setStarted(false); setComment(''); setNotice(''); setLocalError('') }, [selected?.id])

  const transition = useMutation({
    mutationFn: () => reviewApi.transition(selected!.id, 'UNDER_REVIEW'),
    onSuccess: async () => { setStarted(true); setNotice('Review started. Choose a decision and record a comment.'); await cache.invalidateQueries({ queryKey: ['review-queue', user?.id] }) },
    onError: error => setLocalError(error instanceof Error ? error.message : 'The review could not be started.'),
  })
  const decide = useMutation({
    mutationFn: async () => {
      if (!selected || !evidence.data?.evidenceVersionId) throw new Error('There is no evidence version available to review.')
      if (!comment.trim()) throw new Error('Add a comment explaining this decision.')
      return reviewApi.verify(selected.id, evidence.data.evidenceVersionId, action, comment.trim())
    },
    onSuccess: async result => {
      setNotice(`Decision recorded: ${titleCase(result.action)}.`)
      setComment('')
      await Promise.all([cache.invalidateQueries({ queryKey: ['review-queue', user?.id] }), cache.invalidateQueries({ queryKey: ['verification-history', selected?.id] })])
    },
    onError: error => setLocalError(error instanceof Error ? error.message : 'The decision could not be recorded.'),
  })
  const items = queue.data?.pendingReviews ?? []

  if (queue.isLoading) return <PageLoading label="Loading assigned reviews" />
  if (queue.isError) return <PageError error={queue.error} />

  return <div className="page-stack">
    <div className="page-heading"><div><span className="eyebrow">ASSIGNED WORK</span><h1>Review queue</h1><p>Review each submission alongside its work record and evidence, then record a traceable decision.</p></div></div>
    {notice && <SuccessMessage>{notice}</SuccessMessage>}{localError && <div className="alert alert-error" role="alert">{localError}</div>}
    <div className="review-layout">
      <section className="panel review-list"><div className="section-heading"><div><span className="eyebrow">WAITING FOR REVIEW</span><h2>Submissions</h2></div><span className="count-label">{items.length}</span></div>
        {items.length === 0 ? <EmptyState title="No submissions in the queue">New submissions from students assigned to you will appear here.</EmptyState> : <ul className="queue-list">{items.map(item => <li key={item.id}><button className={`queue-item ${selected?.id === item.id ? 'selected' : ''}`} onClick={() => { setParams({ submission: item.id }); setComment(''); setNotice(''); setLocalError('') }}><span className="queue-date">{formatDate(item.work_date)}</span><span className="queue-student">Student <code>{item.user_id}</code></span><span className="queue-meta">{formatHours(item.duration_minutes / 60)} · <Status value={item.status} /></span></button></li>)}</ul>}
      </section>
      {selected ? <ReviewWorkspace key={selected.id} item={selected} workEntry={workEntry.data} activityName={activities.data?.find(activity => activity.id === workEntry.data?.activityTypeId)?.name} entryLoading={workEntry.isLoading} evidence={evidence.data} evidenceLoading={evidence.isLoading} history={history.data ?? []} historyLoading={history.isLoading} started={started || selected.status === 'UNDER_REVIEW'} starting={transition.isPending} action={action} setAction={setAction} comment={comment} setComment={setComment} deciding={decide.isPending} onStart={() => transition.mutate()} onDecide={() => decide.mutate()} /> : <section className="panel review-detail"><EmptyState title="Select a submission">Choose an item from the queue to see its work record and review history.</EmptyState></section>}
    </div>
    {queue.data?.attendanceExceptions.length ? <section className="panel"><div className="section-heading"><div><span className="eyebrow">ATTENDANCE</span><h2>Exceptions requiring attention</h2></div></div><div className="table-wrap"><table><thead><tr><th>Student reference</th><th>Campus reference</th><th>Clock in</th><th>Session</th></tr></thead><tbody>{queue.data.attendanceExceptions.map((item, index) => <tr key={`${String(item.id)}-${index}`}><td className="mono">{String(item.user_id ?? '—')}</td><td className="mono">{String(item.campus_id ?? '—')}</td><td>{item.clock_in_at ? formatTimestamp(String(item.clock_in_at)) : '—'}</td><td><Status value={String(item.status ?? 'UNKNOWN')} /></td></tr>)}</tbody></table></div></section> : null}
  </div>
}

function ReviewWorkspace({ item, workEntry, activityName, entryLoading, evidence, evidenceLoading, history, historyLoading, started, starting, action, setAction, comment, setComment, deciding, onStart, onDecide }: {
  item: QueueItem; workEntry?: Awaited<ReturnType<typeof workApi.entry>>; activityName?: string; entryLoading: boolean
  evidence?: Awaited<ReturnType<typeof workApi.evidence>>; evidenceLoading: boolean
  history: Awaited<ReturnType<typeof reviewApi.verificationHistory>>; historyLoading: boolean
  started: boolean; starting: boolean; action: 'APPROVE' | 'REQUEST_CHANGES' | 'REJECT'
  setAction: (action: 'APPROVE' | 'REQUEST_CHANGES' | 'REJECT') => void
  comment: string; setComment: (comment: string) => void; deciding: boolean
  onStart: () => void; onDecide: () => void
}) {
  const [fileError, setFileError] = useState('')
  async function download() {
    if (!evidence) return
    try {
      const blob = await api.download(`/work-entries/${encodeURIComponent(item.work_entry_id)}/evidence/${encodeURIComponent(evidence.privateObjectReferenceId)}/content`)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = `submission-evidence-v${evidence.versionNumber}`
      anchor.click()
      URL.revokeObjectURL(url)
    } catch (error) { setFileError(error instanceof Error ? error.message : 'The private file could not be opened.') }
  }
  return <section className="panel review-detail"><div className="section-heading"><div><span className="eyebrow">SUBMISSION REVIEW</span><h2>Review work</h2></div><Status value={item.status} /></div>
    <div className="review-summary"><div><span>Student reference</span><strong className="mono">{item.user_id}</strong></div><div><span>Work date</span><strong>{formatDate(item.work_date)}</strong></div><div><span>Recorded duration</span><strong>{formatHours(item.duration_minutes / 60)}</strong></div><div><span>Submission state</span><strong>{titleCase(item.status)}</strong></div><div><span>Activity</span><strong>{entryLoading ? 'Loading…' : activityName ?? 'Unavailable'}</strong></div><div><span>Recorded time</span><strong>{workEntry ? `${workEntry.startTime}–${workEntry.endTime}` : '—'}</strong></div></div>
    <div className="review-block"><div className="review-block-title"><h3>Evidence</h3><span className="muted">Work entry <code>{item.work_entry_id}</code></span></div>
      {evidenceLoading ? <PageLoading label="Loading evidence metadata" /> : evidence ? <div className="evidence-review"><div className="file-glyph"><Eye size={18} /></div><div className="file-info"><strong>{evidence.purpose || 'Work evidence'}</strong><span>Version {evidence.versionNumber} · {evidence.mediaType} · {(evidence.sizeBytes / 1024).toFixed(0)} KB</span><span>Uploaded {formatTimestamp(evidence.uploadedAt)}</span></div><button className="button button-secondary button-small" onClick={download}><Download size={14} /> Download private file</button></div> : <EmptyState title="Evidence is not available">This submission has no uploaded evidence metadata. Ask the student to attach evidence before making a verification decision.</EmptyState>}
      {fileError && <div className="alert alert-error" role="alert">{fileError}</div>}
    </div>
    <div className="review-block"><div className="review-block-title"><h3>Decision history</h3></div>{historyLoading ? <PageLoading label="Loading review history" /> : history.length === 0 ? <p className="muted">No decisions have been recorded.</p> : <ol className="history-list">{history.map(row => <li key={row.id}><div className="history-marker" /><div><strong>{titleCase(row.action)}</strong><p>{row.comment}</p><span>{formatTimestamp(row.createdAt)} · reviewer <code>{row.verifierId}</code></span></div></li>)}</ol>}</div>
    <div className="review-block decision-block"><div className="review-block-title"><h3>Record a decision</h3></div>{!started ? <><p className="muted">Move this item into review before recording a decision.</p><button className="button button-primary" disabled={starting} onClick={onStart}>{starting ? 'Starting review…' : 'Start review'}</button></> : !evidence ? <p className="muted">A versioned evidence file is required before a decision can be recorded.</p> : <div className="decision-form"><label className="field"><span>Decision</span><select value={action} onChange={event => setAction(event.target.value as typeof action)}><option value="APPROVE">Approve</option><option value="REQUEST_CHANGES">Request changes</option><option value="REJECT">Reject</option></select></label><label className="field"><span>Comment</span><textarea rows={3} value={comment} onChange={event => setComment(event.target.value)} placeholder="Explain the decision and any next steps" maxLength={2000} /></label><div className="form-actions"><button className={`button ${action === 'REJECT' ? 'button-danger' : 'button-primary'}`} disabled={deciding || !comment.trim()} onClick={onDecide}><Check size={16} />{deciding ? 'Recording…' : 'Record decision'}</button><span className="field-hint"><MessageSquareText size={14} /> Every decision is retained in the verification history.</span></div></div>}</div>
  </section>
}
