import { afterEach, describe, expect, it, vi } from 'vitest'
import { workApi } from './resources'

describe('dashboard API adapter', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('normalizes JDBC snake_case rows to the frontend dashboard shape', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({
      progress: { targetHours: 18, loggedHours: 4, verifiedHours: 2, pendingHours: 2, remainingHours: 16, percentage: 22, status: 'BELOW_TARGET' },
      attendance: [{ id: 'session-1', status: 'COMPLETED', clock_in_at: '2026-09-28T07:00:00Z', duration_minutes: 90, reconciliation_reference: 'entries=1' }],
      workEntries: [{ id: 'entry-1', work_date: '2026-09-28', duration_minutes: 120, status: 'DRAFT', submission_status: 'NOT_SUBMITTED' }],
      notifications: [],
    }), { status: 200, headers: { 'content-type': 'application/json' } })))

    await expect(workApi.dashboard('student-1', 'period-1')).resolves.toMatchObject({
      attendance: [{ id: 'session-1', clockInAt: '2026-09-28T07:00:00Z', durationMinutes: 90, reconciliationReference: 'entries=1' }],
      workEntries: [{ id: 'entry-1', workDate: '2026-09-28', durationMinutes: 120, submission_status: 'NOT_SUBMITTED' }],
    })
  })
})
