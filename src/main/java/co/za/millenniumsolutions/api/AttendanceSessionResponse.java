package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.AttendanceSession;

import java.time.Instant;

public record AttendanceSessionResponse(String id, String userId, String campusId, String workPeriodId,
                                       Instant clockInAt, Instant clockOutAt, Integer durationMinutes,
                                       String status, String reconciliationReference) {
    public static AttendanceSessionResponse from(AttendanceSession x) {
        return new AttendanceSessionResponse(x.id(), x.userId(), x.campusId(), x.workPeriodId(),
                x.clockInAt(), x.clockOutAt(), x.durationMinutes(), x.status(), x.reconciliationReference());
    }
}
