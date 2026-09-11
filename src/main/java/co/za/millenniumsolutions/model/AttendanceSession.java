package co.za.millenniumsolutions.model;

import java.time.Instant;

import java.util.Objects;

public final class AttendanceSession {
    private final String id;
    private final String userId;
    private final String campusId;
    private final String workPeriodId;
    private final Instant clockInAt;
    private final Instant clockOutAt;
    private final Integer durationMinutes;
    private final String status;
    private final String reconciliationReference;

    public AttendanceSession(String id, String userId, String campusId, String workPeriodId, Instant clockInAt, Instant clockOutAt, Integer durationMinutes, String status, String reconciliationReference) {
        this.id = id;
        this.userId = userId;
        this.campusId = campusId;
        this.workPeriodId = workPeriodId;
        this.clockInAt = clockInAt;
        this.clockOutAt = clockOutAt;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.reconciliationReference = reconciliationReference;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String userId() { return userId; }
    public String getUserId() { return userId; }

    public String campusId() { return campusId; }
    public String getCampusId() { return campusId; }

    public String workPeriodId() { return workPeriodId; }
    public String getWorkPeriodId() { return workPeriodId; }

    public Instant clockInAt() { return clockInAt; }
    public Instant getClockInAt() { return clockInAt; }

    public Instant clockOutAt() { return clockOutAt; }
    public Instant getClockOutAt() { return clockOutAt; }

    public Integer durationMinutes() { return durationMinutes; }
    public Integer getDurationMinutes() { return durationMinutes; }

    public String status() { return status; }
    public String getStatus() { return status; }

    public String reconciliationReference() { return reconciliationReference; }
    public String getReconciliationReference() { return reconciliationReference; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AttendanceSession other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(userId, other.userId) && Objects.equals(campusId, other.campusId) && Objects.equals(workPeriodId, other.workPeriodId) && Objects.equals(clockInAt, other.clockInAt) && Objects.equals(clockOutAt, other.clockOutAt) && Objects.equals(durationMinutes, other.durationMinutes) && Objects.equals(status, other.status) && Objects.equals(reconciliationReference, other.reconciliationReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, campusId, workPeriodId, clockInAt, clockOutAt, durationMinutes, status, reconciliationReference);
    }

    @Override
    public String toString() {
        return "AttendanceSession[id=" + id + ", userId=" + userId + ", campusId=" + campusId + ", workPeriodId=" + workPeriodId + ", clockInAt=" + clockInAt + ", clockOutAt=" + clockOutAt + ", durationMinutes=" + durationMinutes + ", status=" + status + ", reconciliationReference=" + reconciliationReference + "]";
    }
}
