-- Tests use a dedicated SQLite file under target/, never the local development database.
-- Delete dependent rows first because foreign keys are enabled in the test connection.
DELETE FROM verification;
DELETE FROM submission;
DELETE FROM evidence_version;
DELETE FROM evidence;
DELETE FROM attendance_correction;
DELETE FROM attendance_capture;
DELETE FROM attendance_session;
DELETE FROM work_entry_timing;
DELETE FROM work_entry;
DELETE FROM audit_log;
DELETE FROM notification;
DELETE FROM escalation;
DELETE FROM auth_session;
DELETE FROM private_object_reference;
UPDATE app_user SET mentor_id = NULL, supervisor_id = NULL;
DELETE FROM app_user;
DELETE FROM campus_geofence;
DELETE FROM campus;
DELETE FROM institution;
DELETE FROM activity_type;
DELETE FROM work_period;
DELETE FROM work_role;
