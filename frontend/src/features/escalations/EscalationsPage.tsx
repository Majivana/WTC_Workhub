import { useState } from 'react'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { escalationApi } from '../../api/resources'
import { EmptyState, PageError, PageLoading, Status, SuccessMessage } from '../../components/ui/Feedback'
import { formatTimestamp } from '../../utils/format'

const escalationSchema = z.object({ subjectType: z.string().min(1), subjectId: z.string().min(1), severity: z.enum(['LOW', 'MEDIUM', 'HIGH']), reason: z.string().min(10, 'Provide a short description of at least 10 characters.').max(2000) })
type EscalationForm = z.infer<typeof escalationSchema>

export function EscalationsPage() {
  const cache = useQueryClient()
  const [created, setCreated] = useState(false)
  const list = useQuery({ queryKey: ['escalations'], queryFn: escalationApi.list })
  const form = useForm<EscalationForm>({ resolver: zodResolver(escalationSchema), defaultValues: { subjectType: 'WORK_ENTRY', subjectId: '', severity: 'MEDIUM', reason: '' } })
  const create = useMutation({ mutationFn: (values: EscalationForm) => escalationApi.create(values), onSuccess: async () => { setCreated(true); form.reset(); await cache.invalidateQueries({ queryKey: ['escalations'] }) } })
  if (list.isLoading) return <PageLoading label="Loading escalations" />
  if (list.isError) return <PageError error={list.error} />
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">OPERATIONAL FOLLOW UP</span><h1>Escalations</h1><p>Raise and track unresolved issues that need supervisor or mentor attention.</p></div></div>
    {created && <SuccessMessage>Escalation recorded for follow-up.</SuccessMessage>}{create.isError && <PageError error={create.error} />}
    <div className="two-column-layout"><section className="panel"><div className="section-heading"><div><span className="eyebrow">OPEN ITEMS</span><h2>Unresolved escalations</h2></div><span className="muted">{list.data?.length ?? 0}</span></div>{!list.data?.length ? <EmptyState title="No open escalations">New unresolved issues will be listed here.</EmptyState> : <div className="table-wrap"><table><thead><tr><th>Issue</th><th>Severity</th><th>Status</th><th>Opened</th></tr></thead><tbody>{list.data.map(item => <tr key={item.id}><td><strong>{item.subjectType}</strong><div className="muted">{item.reason}</div><code>{item.subjectId}</code></td><td><Status value={item.severity} /></td><td><Status value={item.status} /></td><td>{formatTimestamp(item.createdAt)}</td></tr>)}</tbody></table></div>}</section>
      <section className="panel"><div className="section-heading"><div><span className="eyebrow">CREATE</span><h2>Raise an issue</h2></div></div><form className="form-stack" onSubmit={form.handleSubmit(values => { setCreated(false); create.mutate(values) })}><label className="field"><span>Related record type</span><select {...form.register('subjectType')}><option value="WORK_ENTRY">Work entry</option><option value="ATTENDANCE_SESSION">Attendance session</option><option value="SUBMISSION">Submission</option></select></label><label className="field"><span>Record ID</span><input {...form.register('subjectId')} placeholder="Paste the Workhub record ID" /><FieldError value={form.formState.errors.subjectId?.message} /></label><label className="field"><span>Severity</span><select {...form.register('severity')}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option></select></label><label className="field"><span>Describe the issue</span><textarea rows={5} {...form.register('reason')} maxLength={2000} /><FieldError value={form.formState.errors.reason?.message} /></label><button className="button button-primary" disabled={create.isPending}>{create.isPending ? 'Submitting…' : 'Submit escalation'}</button></form></section>
    </div>
  </div>
}
function FieldError({ value }: { value?: string }) { return value ? <small className="field-error">{value}</small> : null }
