package co.za.millenniumsolutions.model;

import java.time.Instant;

public record AttendanceCapture(String id, String attendanceSessionId, String captureType,
                                Instant capturedAt, String privateObjectReferenceId,
                                boolean locationValid, String locationResult) {}
