package co.za.millenniumsolutions.model;

import java.time.Instant;

public record AttendanceSession(String id, String userId, String campusId, String workPeriodId,
                                Instant clockInAt, Instant clockOutAt, Integer durationMinutes,
                                String status, String reconciliationReference) {}
