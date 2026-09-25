export type Role = 'STUDENT' | 'SUPERVISOR' | 'MENTOR' | 'ADMIN' | 'SUPER_ADMIN'

export interface CurrentUser {
  id: string
  username: string
  displayName: string
  systemRole: Role
  institutionId?: string | null
  campusId?: string | null
  workRoleId?: string | null
  mentorId?: string | null
  supervisorId?: string | null
  active: boolean
  permissions: string[]
}

export interface LoginResponse {
  token: string
  userId: string
  username: string
  role: Role
  expiresAt: string
}

export interface WorkPeriod {
  id: string
  name: string
  startDate: string
  endDate: string
  weeklyHoursTarget: number
}

export interface Progress {
  targetHours: number
  loggedHours: number
  verifiedHours: number
  pendingHours: number
  remainingHours: number
  percentage: number
  status: 'BELOW_TARGET' | 'ON_TARGET' | 'OVER_TARGET'
}

export interface WorkEntry {
  id: string
  userId: string
  workPeriodId: string
  activityTypeId: string
  workDate: string
  startTime: string
  endTime: string
  breakMinutes: number
  durationMinutes: number
  status: string
}

export interface ActivityType {
  id: string
  name: string
  active: boolean
}

export interface AttendanceSession {
  id: string
  userId: string
  campusId: string
  workPeriodId: string
  clockInAt: string
  clockOutAt?: string | null
  durationMinutes?: number | null
  status: string
  reconciliationReference?: string | null
}

export interface Notification {
  id: string
  recipientId: string
  type: string
  title: string
  message: string
  entityType?: string | null
  entityId?: string | null
  readAt?: string | null
  createdAt: string
}

export interface StudentDashboard {
  progress: Progress
  attendance: AttendanceSession[]
  workEntries: Array<WorkEntry & { submission_status?: string }>
  notifications: Notification[]
}

export interface QueueItem {
  id: string
  work_entry_id: string
  status: string
  user_id: string
  work_date: string
  duration_minutes: number
}

export interface SupervisorQueue {
  pendingReviews: QueueItem[]
  attendanceExceptions: Array<Record<string, unknown>>
  escalations: Escalation[]
}

export interface Submission {
  id: string
  workEntryId: string
  status: string
}

export interface EvidenceMetadata {
  evidenceId: string
  workEntryId: string
  status: string
  versionNumber: number
  objectKey: string
  mediaType: string
  sizeBytes: number
  checksum: string
  changeNotes?: string | null
  purpose: string
  createdBy: string
  uploadedAt: string
  uploadUrl?: string | null
  presigned: boolean
}

export interface Verification {
  id: string
  submissionId: string
  verifierId: string
  evidenceVersionId: string
  action: 'APPROVE' | 'REQUEST_CHANGES' | 'REJECT'
  comment: string
  createdAt: string
}

export interface Escalation {
  id: string
  subjectType: string
  subjectId: string
  openedBy: string
  severity: 'LOW' | 'MEDIUM' | 'HIGH'
  status: string
  reason: string
  assignedTo?: string | null
  resolvedAt?: string | null
  createdAt: string
}

export interface Institution { id: string; name: string }
export interface Campus { id: string; institutionId: string; name: string }
export interface User {
  id: string; username: string; displayName: string; systemRole: Role
  institutionId?: string | null; campusId?: string | null; workRoleId?: string | null
  mentorId?: string | null; supervisorId?: string | null; active: boolean
}

