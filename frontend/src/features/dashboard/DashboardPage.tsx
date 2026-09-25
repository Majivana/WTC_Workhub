import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, CalendarDays, Clock3, FileCheck2, Plus, UsersRound } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../../app/auth'
import { reviewApi, workApi } from '../../api/resources'
import { EmptyState, PageError, PageLoading, Status } from '../../components/ui/Feedback'
import type { WorkPeriod } from '../../types/domain'
import { formatDate, formatHours, formatTimestamp } from '../../utils/format'

export function DashboardPage() {
  const { user, hasRole, has } = useAuth()
  const canReview = has('SUBMISSION_REVIEW', 'VERIFICATION_REVIEW') || hasRole('SUPERVISOR', 'MENTOR', 'ADMIN', 'SUPER_ADMIN')
  const periods = useQuery({ queryKey: ['work-periods'], queryFn: workApi.periods })
  const [selectedId, setSelectedId] = useState('')
  const period = useMemo(() => periods.data?.find(item => item.id === selectedId) ?? periods.data?.[0], [periods.data, selectedId])
  const studentDashboard = useQuery({
    queryKey: ['dashboard', user?.id, period?.id],
    queryFn: () => workApi.dashboard(user!.id, period!.id),
    enabled: !!user && hasRole('STUDENT') && !!period,
  })
  const queue = useQuery({ queryKey: ['review-queue', user?.id], queryFn: () => reviewApi.queue(user!.id), enabled: !!user && canReview })

  if (periods.isLoading) return <PageLoading label="Loading your workspace" />
  if (periods.isError) return <PageError error={periods.error} />

  return <div className="page-stack">
    <div className="page-heading"><div><span className="eyebrow">{formatDate(new Date().toISOString().slice(0, 10))}</span><h1>{canReview && !hasRole('STUDENT') ? 'Review overview' : 'Work overview'}</h1><p>{hasRole('STUDENT') ? 'Your attendance, recorded work and progress for the selected period.' : canReview ? 'Items assigned for review and attendance exceptions.' : 'Operational workspace for your assigned responsibilities.'}</p></div>{hasRole('STUDENT') && <Link className="button button-primary" to="/app/work"><Plus size={17} /> Record work</Link>}</div>

    {hasRole('STUDENT') && <>
      {!period ? <EmptyState title="No work periods are configured">A work period must be available before progress and work entries can be shown.</EmptyState> : <>
        <section className="period-bar"><div><CalendarDays size={17} /><div><strong>{period.name}</strong><span>{formatDate(period.startDate)} – {formatDate(period.endDate)}</span></div></div><label className="compact-select"><span className="sr-only">Work period</span><select value={period.id} onChange={event => setSelectedId(event.target.value)}>{periods.data?.map((item: WorkPeriod) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label></section>
        {studentDashboard.isLoading ? <PageLoading label="Loading period activity" /> : studentDashboard.isError ? <PageError error={studentDashboard.error} /> : studentDashboard.data && <>
          <section className="progress-section" aria-label="Weekly progress">
            <div className="section-heading"><div><span className="eyebrow">CURRENT PERIOD</span><h2>Weekly progress</h2></div><Status value={studentDashboard.data.progress.status} /></div>
            <div className="progress-main"><div className="progress-value"><strong>{formatHours(studentDashboard.data.progress.loggedHours)}</strong><span>logged</span></div><div className="progress-track" role="progressbar" aria-label="Logged hours against weekly target" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.min(100, Math.max(0, studentDashboard.data.progress.percentage))}><span style={{ width: `${Math.min(100, Math.max(0, studentDashboard.data.progress.percentage))}%` }} /></div><div className="progress-target"><span>{formatHours(studentDashboard.data.progress.verifiedHours)} verified</span><span>{formatHours(studentDashboard.data.progress.targetHours)} weekly target</span></div></div>
            <div className="progress-details"><div><span>Awaiting review</span><strong>{formatHours(studentDashboard.data.progress.pendingHours)}</strong></div><div><span>Remaining this period</span><strong>{formatHours(studentDashboard.data.progress.remainingHours)}</strong></div></div>
          </section>
          <div className="dashboard-columns">
            <section className="panel"><div className="section-heading"><div><span className="eyebrow">RECORD</span><h2>Recent work</h2></div><Link className="text-link" to="/app/work">View all <ArrowRight size={14} /></Link></div>
              {studentDashboard.data.workEntries.length === 0 ? <EmptyState title="No work recorded for this period" action={<Link className="button button-secondary" to="/app/work">Create a work entry</Link>}>Record your hours against an activity when your work is complete.</EmptyState> : <div className="table-wrap"><table><thead><tr><th>Date</th><th>Duration</th><th>Work status</th><th>Submission</th></tr></thead><tbody>{studentDashboard.data.workEntries.slice(0, 6).map(entry => <tr key={entry.id}><td>{formatDate(entry.workDate)}</td><td>{formatHours(entry.durationMinutes / 60)}</td><td><Status value={entry.status} /></td><td>{entry.submission_status ? <Status value={entry.submission_status} /> : '—'}</td></tr>)}</tbody></table></div>}
            </section>
            <section className="panel"><div className="section-heading"><div><span className="eyebrow">ATTENDANCE</span><h2>Recent sessions</h2></div><Link className="text-link" to="/app/attendance">Attendance <ArrowRight size={14} /></Link></div>
              {studentDashboard.data.attendance.length === 0 ? <EmptyState title="No sessions recorded">Clock in at your assigned campus to start a session.</EmptyState> : <ul className="activity-list">{studentDashboard.data.attendance.slice(0, 5).map((session, index) => <li key={`${session.id}-${index}`}><div className="activity-icon"><Clock3 size={17} /></div><div className="activity-copy"><strong>{formatTimestamp(session.clockInAt)}</strong><span>{session.durationMinutes ? formatHours(session.durationMinutes / 60) : 'Session in progress'}</span></div><Status value={session.status} /></li>)}</ul>}
            </section>
          </div>
          <section className="panel"><div className="section-heading"><div><span className="eyebrow">FOLLOW UP</span><h2>Notifications</h2></div><Link className="text-link" to="/app/notifications">Open inbox <ArrowRight size={14} /></Link></div>
            {studentDashboard.data.notifications.length === 0 ? <EmptyState title="You're up to date">Updates about reviews and attendance will appear here.</EmptyState> : <ul className="notification-list">{studentDashboard.data.notifications.slice(0, 4).map(item => <li key={item.id} className={!item.readAt ? 'unread' : ''}><div><strong>{item.title}</strong><p>{item.message}</p></div><time>{formatTimestamp(item.createdAt)}</time></li>)}</ul>}
          </section>
        </>}
      </>}
    </>}

    {canReview && <section className="panel"><div className="section-heading"><div><span className="eyebrow">REVIEW QUEUE</span><h2>Pending reviews</h2></div><Link className="text-link" to="/app/review">Open queue <ArrowRight size={14} /></Link></div>{queue.isLoading ? <PageLoading label="Loading assigned reviews" /> : queue.isError ? <PageError error={queue.error} /> : queue.data?.pendingReviews.length ? <div className="table-wrap"><table><thead><tr><th>Work date</th><th>Student reference</th><th>Duration</th><th>Status</th><th></th></tr></thead><tbody>{queue.data.pendingReviews.slice(0, 8).map(item => <tr key={item.id}><td>{formatDate(item.work_date)}</td><td className="mono">{item.user_id}</td><td>{formatHours(item.duration_minutes / 60)}</td><td><Status value={item.status} /></td><td><Link className="text-link" to={`/app/review?submission=${encodeURIComponent(item.id)}`}>Review <ArrowRight size={14} /></Link></td></tr>)}</tbody></table></div> : <EmptyState title="No reviews waiting">Assigned submissions requiring a decision will appear here.</EmptyState>}</section>}

    {hasRole('ADMIN', 'SUPER_ADMIN') && <section className="panel"><div className="section-heading"><div><span className="eyebrow">OPERATIONS</span><h2>Administration</h2></div></div><div className="quick-links"><Link to="/app/administration"><UserRoundIcon /> Manage users and campus settings <ArrowRight size={16} /></Link><Link to="/app/reports"><FileCheck2 size={18} /> Download work summary <ArrowRight size={16} /></Link></div></section>}
  </div>
}

function UserRoundIcon() { return <UsersRound size={18} /> }
