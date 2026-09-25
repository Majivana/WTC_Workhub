import { useState } from 'react'
import { Download, FilterX } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { adminApi, workApi } from '../../api/resources'
import { EmptyState, PageError, PageLoading, SuccessMessage } from '../../components/ui/Feedback'
import { formatDate } from '../../utils/format'

export function ReportsPage() {
  const periods = useQuery({ queryKey: ['work-periods'], queryFn: workApi.periods })
  const [filters, setFilters] = useState({ userId: '', workPeriodId: '', status: '' })
  const [downloaded, setDownloaded] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  async function download() {
    setLoading(true); setError(''); setDownloaded(false)
    try {
      const csv = await adminApi.report(filters)
      if (!csv.trim() || csv.trim().split('\n').length <= 1) { setError('The report contains no rows for the selected filters.'); return }
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = `work-summary-${new Date().toISOString().slice(0, 10)}.csv`
      anchor.click()
      URL.revokeObjectURL(url)
      setDownloaded(true)
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'The report could not be downloaded.') }
    finally { setLoading(false) }
  }
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">OPERATIONAL REPORTING</span><h1>Reports</h1><p>Export the supported work summary from the Workhub API as CSV.</p></div></div>
    {periods.isLoading && <PageLoading label="Loading report filters" />}{periods.isError && <PageError error={periods.error} />}
    {downloaded && <SuccessMessage>CSV report downloaded. It contains work and submission status for the selected criteria.</SuccessMessage>}{error && <div className="alert alert-error" role="alert">{error}</div>}
    <section className="panel report-panel"><div className="section-heading"><div><span className="eyebrow">WORK SUMMARY</span><h2>Choose report criteria</h2></div></div><div className="report-explainer">The export includes work-entry ID, user reference, period, date, duration, work status, submission status and verification status. Access is controlled by your account permissions.</div><div className="form-grid report-filters"><label className="field"><span>User ID <small>(optional)</small></span><input value={filters.userId} onChange={event => setFilters({ ...filters, userId: event.target.value })} placeholder="Filter to one user" /></label><label className="field"><span>Work period <small>(optional)</small></span><select value={filters.workPeriodId} onChange={event => setFilters({ ...filters, workPeriodId: event.target.value })}><option value="">All periods</option>{periods.data?.map(item => <option key={item.id} value={item.id}>{item.name} · {formatDate(item.startDate)}</option>)}</select></label><label className="field"><span>Work status <small>(optional)</small></span><select value={filters.status} onChange={event => setFilters({ ...filters, status: event.target.value })}><option value="">All statuses</option><option value="DRAFT">Draft</option><option value="APPROVED">Approved</option><option value="VERIFIED">Verified</option></select></label></div><div className="form-actions"><button className="button button-primary" disabled={loading || periods.isLoading} onClick={download}><Download size={16} /> {loading ? 'Preparing report…' : 'Download CSV'}</button><button className="button button-quiet" onClick={() => { setFilters({ userId: '', workPeriodId: '', status: '' }); setError(''); setDownloaded(false) }}><FilterX size={15} /> Clear filters</button></div></section>
    <EmptyState title="No charts are available">The current API supports a filtered CSV work summary. This page does not invent additional analytics.</EmptyState>
  </div>
}

