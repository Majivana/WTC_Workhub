import { api } from './client'
import type {
  ActivityType, AttendanceSession, Campus, CurrentUser, EvidenceMetadata, Institution,
  LoginResponse, Notification, StudentDashboard, Submission, SupervisorQueue,
  User, Verification, WorkEntry, WorkPeriod,
} from '../types/domain'

export const authApi = {
  login: (username: string, password: string) => api.post<LoginResponse>('/auth/login', { username, password }),
  me: () => api.get<CurrentUser>('/me'),
}
export const workApi = {
  periods: () => api.get<WorkPeriod[]>('/work-periods'),
  activities: () => api.get<ActivityType[]>('/activity-types'),
  dashboard: (userId: string, periodId: string) => api.get<StudentDashboard>(`/users/${encodeURIComponent(userId)}/work-periods/${encodeURIComponent(periodId)}/dashboard`),
  entries: (userId: string, periodId: string) => api.get<WorkEntry[]>(`/users/${encodeURIComponent(userId)}/work-periods/${encodeURIComponent(periodId)}/work-entries`),
  entry: (id: string) => api.get<WorkEntry>(`/work-entries/${encodeURIComponent(id)}`),
  createEntry: (userId: string, periodId: string, input: Pick<WorkEntry, 'activityTypeId' | 'workDate' | 'startTime' | 'endTime' | 'breakMinutes'>) => api.post<WorkEntry>(`/users/${encodeURIComponent(userId)}/work-periods/${encodeURIComponent(periodId)}/work-entries`, input),
  updateEntry: (id: string, input: Pick<WorkEntry, 'activityTypeId' | 'workDate' | 'startTime' | 'endTime' | 'breakMinutes'>) => api.put<WorkEntry>(`/work-entries/${encodeURIComponent(id)}`, input),
  createSubmission: (workEntryId: string) => api.post<Submission>(`/work-entries/${encodeURIComponent(workEntryId)}/submissions`),
  submissionForEntry: (workEntryId: string) => api.get<Submission | null>(`/work-entries/${encodeURIComponent(workEntryId)}/submissions`).catch(error => {
    if (error instanceof Error && 'status' in error && error.status === 404) return null
    throw error
  }),
  transitionSubmission: (id: string, status: string) => api.post<Submission>(`/submissions/${encodeURIComponent(id)}/transitions`, { status }),
  evidence: (workEntryId: string) => api.get<EvidenceMetadata>(`/work-entries/${encodeURIComponent(workEntryId)}/evidence`),
  createEvidence: (workEntryId: string, input: object) => api.post<EvidenceMetadata>(`/work-entries/${encodeURIComponent(workEntryId)}/evidence`, input),
  uploadEvidenceBytes: (workEntryId: string, objectId: string, blob: Blob, mediaType: string) => api.upload<void>(`/work-entries/${encodeURIComponent(workEntryId)}/evidence/${encodeURIComponent(objectId)}/content`, blob, mediaType),
  downloadEvidence: (workEntryId: string, objectId: string) => api.download(`/work-entries/${encodeURIComponent(workEntryId)}/evidence/${encodeURIComponent(objectId)}/content`),
  attendanceIn: (input: object) => api.post<AttendanceSession>('/attendance/clock-in', input),
  attendanceOut: (sessionId: string, input: object) => api.post<AttendanceSession>(`/attendance/${encodeURIComponent(sessionId)}/clock-out`, input),
}
export const reviewApi = {
  queue: (supervisorId: string) => api.get<SupervisorQueue>(`/supervisor/queue?supervisorId=${encodeURIComponent(supervisorId)}`),
  submission: (id: string) => api.get<Submission>(`/submissions/${encodeURIComponent(id)}`),
  verificationHistory: (id: string) => api.get<Verification[]>(`/submissions/${encodeURIComponent(id)}/verifications`),
  verify: (id: string, evidenceVersionId: string, action: string, comment: string) => api.post<Verification>(`/submissions/${encodeURIComponent(id)}/verifications`, { evidenceVersionId, action, comment }),
  transition: (id: string, status: string) => api.post<Submission>(`/submissions/${encodeURIComponent(id)}/transitions`, { status }),
}
export const notificationApi = {
  list: (userId: string) => api.get<Notification[]>(`/users/${encodeURIComponent(userId)}/notifications`),
  markRead: (userId: string, id: string) => api.post<void>(`/users/${encodeURIComponent(userId)}/notifications/${encodeURIComponent(id)}/read`),
}
export const escalationApi = {
  list: () => api.get<import('../types/domain').Escalation[]>('/escalations'),
  create: (input: object) => api.post<import('../types/domain').Escalation>('/escalations', input),
}
export const adminApi = {
  users: (includeInactive = false) => api.get<User[]>(`/admin/users?includeInactive=${includeInactive}`),
  createUser: (input: object) => api.post<User>('/admin/users', input),
  updateUser: (id: string, input: object) => api.put<User>(`/admin/users/${encodeURIComponent(id)}`, input),
  deactivateUser: (id: string) => api.post<void>(`/admin/users/${encodeURIComponent(id)}/deactivate`),
  institutions: () => api.get<Institution[]>('/admin/institutions'),
  campuses: () => api.get<Campus[]>('/admin/campuses'),
  createInstitution: (name: string) => api.post<Institution>('/admin/institutions', { name }),
  createCampus: (institutionId: string, name: string) => api.post<Campus>('/admin/campuses', { institutionId, name }),
  activityTypes: (includeInactive = true) => api.get<ActivityType[]>(`/activity-types?includeInactive=${includeInactive}`),
  createActivityType: (name: string) => api.post<ActivityType>('/activity-types', { name, active: true }),
  report: (filters: Record<string, string>) => {
    const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value))
    return api.get<string>(`/admin/reports/work-summary.csv${query.size ? `?${query}` : ''}`)
  },
}
