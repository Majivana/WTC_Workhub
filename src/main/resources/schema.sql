-- Repeatable SQLite initialization. Object bytes never belong in relational records.
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS institution (
    id TEXT PRIMARY KEY, name TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS campus (
    id TEXT PRIMARY KEY, institution_id TEXT NOT NULL, name TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (institution_id) REFERENCES institution(id),
    UNIQUE (institution_id, name)
);
CREATE TABLE IF NOT EXISTS campus_geofence (
    id TEXT PRIMARY KEY, campus_id TEXT NOT NULL, latitude REAL NOT NULL,
    longitude REAL NOT NULL, radius_metres INTEGER NOT NULL CHECK (radius_metres > 0),
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1)),
    FOREIGN KEY (campus_id) REFERENCES campus(id)
);
CREATE TABLE IF NOT EXISTS work_role (
    id TEXT PRIMARY KEY, code TEXT NOT NULL UNIQUE, name TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS app_user (
    id TEXT PRIMARY KEY, username TEXT NOT NULL UNIQUE, display_name TEXT NOT NULL,
    system_role TEXT NOT NULL, work_role_id TEXT, institution_id TEXT, campus_id TEXT,
    mentor_id TEXT, supervisor_id TEXT, password_hash TEXT,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1)),
    FOREIGN KEY (work_role_id) REFERENCES work_role(id), FOREIGN KEY (institution_id) REFERENCES institution(id),
    FOREIGN KEY (campus_id) REFERENCES campus(id), FOREIGN KEY (mentor_id) REFERENCES app_user(id),
    FOREIGN KEY (supervisor_id) REFERENCES app_user(id)
);
CREATE TABLE IF NOT EXISTS auth_session (
    id TEXT PRIMARY KEY, user_id TEXT NOT NULL, token_hash TEXT NOT NULL UNIQUE,
    expires_at TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TEXT, FOREIGN KEY (user_id) REFERENCES app_user(id)
);
CREATE TABLE IF NOT EXISTS permission (
    code TEXT PRIMARY KEY, description TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS role_permission (
    system_role TEXT NOT NULL, permission_code TEXT NOT NULL,
    PRIMARY KEY (system_role, permission_code),
    FOREIGN KEY (permission_code) REFERENCES permission(code)
);
CREATE TABLE IF NOT EXISTS activity_type (
    id TEXT PRIMARY KEY, name TEXT NOT NULL UNIQUE,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1))
);
CREATE TABLE IF NOT EXISTS work_period (
    id TEXT PRIMARY KEY, name TEXT NOT NULL UNIQUE, start_date TEXT NOT NULL, end_date TEXT NOT NULL,
    weekly_hours_target REAL NOT NULL CHECK (weekly_hours_target >= 0), CHECK (end_date >= start_date)
);
CREATE TABLE IF NOT EXISTS work_entry (
    id TEXT PRIMARY KEY, user_id TEXT NOT NULL, work_period_id TEXT NOT NULL,
    activity_type_id TEXT NOT NULL, work_date TEXT NOT NULL, duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    status TEXT NOT NULL, FOREIGN KEY (user_id) REFERENCES app_user(id),
    FOREIGN KEY (work_period_id) REFERENCES work_period(id), FOREIGN KEY (activity_type_id) REFERENCES activity_type(id)
);
CREATE TABLE IF NOT EXISTS work_entry_timing (
    work_entry_id TEXT PRIMARY KEY, start_time TEXT NOT NULL, end_time TEXT NOT NULL,
    break_minutes INTEGER NOT NULL DEFAULT 0 CHECK (break_minutes >= 0),
    FOREIGN KEY (work_entry_id) REFERENCES work_entry(id) ON DELETE CASCADE,
    CHECK (end_time > start_time)
);
CREATE TABLE IF NOT EXISTS attendance_session (
    id TEXT PRIMARY KEY, user_id TEXT NOT NULL, campus_id TEXT NOT NULL, work_period_id TEXT,
    clock_in_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, clock_out_at TEXT,
    duration_minutes INTEGER CHECK (duration_minutes IS NULL OR duration_minutes >= 0),
    status TEXT NOT NULL, reconciliation_reference TEXT,
    FOREIGN KEY (user_id) REFERENCES app_user(id), FOREIGN KEY (campus_id) REFERENCES campus(id),
    FOREIGN KEY (work_period_id) REFERENCES work_period(id), CHECK (clock_out_at IS NULL OR clock_out_at >= clock_in_at)
);
CREATE TABLE IF NOT EXISTS private_object_reference (
    id TEXT PRIMARY KEY, object_key TEXT NOT NULL UNIQUE, media_type TEXT NOT NULL,
    size_bytes INTEGER NOT NULL CHECK (size_bytes >= 0), checksum TEXT, purpose TEXT NOT NULL,
    created_by TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES app_user(id)
);
CREATE TABLE IF NOT EXISTS attendance_capture (
    id TEXT PRIMARY KEY, attendance_session_id TEXT NOT NULL, capture_type TEXT NOT NULL,
    captured_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, private_object_reference_id TEXT NOT NULL,
    location_valid INTEGER NOT NULL CHECK (location_valid IN (0,1)), location_result TEXT NOT NULL,
    FOREIGN KEY (attendance_session_id) REFERENCES attendance_session(id),
    FOREIGN KEY (private_object_reference_id) REFERENCES private_object_reference(id)
);
CREATE TABLE IF NOT EXISTS attendance_correction (
    id TEXT PRIMARY KEY, attendance_session_id TEXT NOT NULL, actor_id TEXT NOT NULL,
    reason TEXT NOT NULL, old_clock_in_at TEXT, old_clock_out_at TEXT,
    new_clock_in_at TEXT, new_clock_out_at TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_session_id) REFERENCES attendance_session(id),
    FOREIGN KEY (actor_id) REFERENCES app_user(id)
);
CREATE TABLE IF NOT EXISTS evidence (
    id TEXT PRIMARY KEY, work_entry_id TEXT NOT NULL, status TEXT NOT NULL,
    FOREIGN KEY (work_entry_id) REFERENCES work_entry(id), UNIQUE (work_entry_id)
);
CREATE TABLE IF NOT EXISTS evidence_version (
    id TEXT PRIMARY KEY, evidence_id TEXT NOT NULL, version_number INTEGER NOT NULL CHECK (version_number > 0),
    private_object_reference_id TEXT NOT NULL, checksum TEXT NOT NULL, change_notes TEXT,
    uploaded_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evidence_id) REFERENCES evidence(id), FOREIGN KEY (private_object_reference_id) REFERENCES private_object_reference(id),
    UNIQUE (evidence_id, version_number)
);
CREATE TABLE IF NOT EXISTS submission (
    id TEXT PRIMARY KEY, work_entry_id TEXT NOT NULL, status TEXT NOT NULL,
    FOREIGN KEY (work_entry_id) REFERENCES work_entry(id), UNIQUE (work_entry_id)
);
CREATE TABLE IF NOT EXISTS verification (
    id TEXT PRIMARY KEY, submission_id TEXT NOT NULL, verifier_id TEXT NOT NULL,
    evidence_version_id TEXT, action TEXT NOT NULL, comment TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submission(id), FOREIGN KEY (verifier_id) REFERENCES app_user(id),
    FOREIGN KEY (evidence_version_id) REFERENCES evidence_version(id)
);
CREATE TABLE IF NOT EXISTS audit_log (
    id TEXT PRIMARY KEY, actor_id TEXT, entity_type TEXT NOT NULL, entity_id TEXT NOT NULL,
    action TEXT NOT NULL, details TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES app_user(id)
);
CREATE TABLE IF NOT EXISTS notification (
    id TEXT PRIMARY KEY, recipient_id TEXT NOT NULL, type TEXT NOT NULL,
    title TEXT NOT NULL, message TEXT NOT NULL, entity_type TEXT, entity_id TEXT,
    read_at TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES app_user(id)
);
CREATE TABLE IF NOT EXISTS escalation (
    id TEXT PRIMARY KEY, subject_type TEXT NOT NULL, subject_id TEXT NOT NULL,
    opened_by TEXT NOT NULL, severity TEXT NOT NULL, status TEXT NOT NULL,
    reason TEXT NOT NULL, assigned_to TEXT, resolved_at TEXT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (opened_by) REFERENCES app_user(id), FOREIGN KEY (assigned_to) REFERENCES app_user(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_active_attendance_session_user ON attendance_session(user_id) WHERE status = 'ACTIVE';
CREATE UNIQUE INDEX IF NOT EXISTS ux_submission_work_entry ON submission(work_entry_id);
CREATE INDEX IF NOT EXISTS ix_campus_geofence_campus_active ON campus_geofence(campus_id, active);
CREATE INDEX IF NOT EXISTS ix_work_entry_user_period_date ON work_entry(user_id, work_period_id, work_date);
CREATE INDEX IF NOT EXISTS ix_work_entry_timing_range ON work_entry_timing(start_time, end_time);
CREATE INDEX IF NOT EXISTS ix_attendance_session_user_status ON attendance_session(user_id, status);
CREATE INDEX IF NOT EXISTS ix_attendance_capture_session ON attendance_capture(attendance_session_id);
CREATE INDEX IF NOT EXISTS ix_attendance_correction_session ON attendance_correction(attendance_session_id, created_at);
CREATE INDEX IF NOT EXISTS ix_evidence_version_evidence ON evidence_version(evidence_id, version_number);
CREATE INDEX IF NOT EXISTS ix_verification_submission_created ON verification(submission_id, created_at);
CREATE INDEX IF NOT EXISTS ix_audit_log_entity_created ON audit_log(entity_type, entity_id, created_at);
CREATE INDEX IF NOT EXISTS ix_auth_session_token ON auth_session(token_hash);
CREATE INDEX IF NOT EXISTS ix_notification_recipient_created ON notification(recipient_id, created_at);
CREATE UNIQUE INDEX IF NOT EXISTS ux_weekly_progress_notification ON notification(recipient_id, type, entity_id)
    WHERE type = 'WEEKLY_PROGRESS_REMINDER' AND entity_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_escalation_status_severity ON escalation(status, severity, created_at);
