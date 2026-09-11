package co.za.millenniumsolutions.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public final class WorkEntry {
    private final String id;
    private final String userId;
    private final String workPeriodId;
    private final String activityTypeId;
    private final LocalDate workDate;
    private final int durationMinutes;
    private final String status;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int breakMinutes;

    public WorkEntry(String id, String userId, String workPeriodId, String activityTypeId,
                     LocalDate workDate, int durationMinutes, String status,
                     LocalTime startTime, LocalTime endTime, int breakMinutes) {
        this.id = id;
        this.userId = userId;
        this.workPeriodId = workPeriodId;
        this.activityTypeId = activityTypeId;
        this.workDate = workDate;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
    }

    public WorkEntry(String id, String userId, String workPeriodId, String activityTypeId,
                     LocalDate workDate, int durationMinutes, String status) {
        this(id, userId, workPeriodId, activityTypeId, workDate, durationMinutes, status,
                null, null, 0);
    }

    public String id() { return id; }
    public String getId() { return id; }
    public String userId() { return userId; }
    public String getUserId() { return userId; }
    public String workPeriodId() { return workPeriodId; }
    public String getWorkPeriodId() { return workPeriodId; }
    public String activityTypeId() { return activityTypeId; }
    public String getActivityTypeId() { return activityTypeId; }
    public LocalDate workDate() { return workDate; }
    public LocalDate getWorkDate() { return workDate; }
    public int durationMinutes() { return durationMinutes; }
    public int getDurationMinutes() { return durationMinutes; }
    public String status() { return status; }
    public String getStatus() { return status; }
    public LocalTime startTime() { return startTime; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime endTime() { return endTime; }
    public LocalTime getEndTime() { return endTime; }
    public int breakMinutes() { return breakMinutes; }
    public int getBreakMinutes() { return breakMinutes; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WorkEntry other)) return false;
        return durationMinutes == other.durationMinutes && breakMinutes == other.breakMinutes
                && Objects.equals(id, other.id) && Objects.equals(userId, other.userId)
                && Objects.equals(workPeriodId, other.workPeriodId)
                && Objects.equals(activityTypeId, other.activityTypeId)
                && Objects.equals(workDate, other.workDate) && Objects.equals(status, other.status)
                && Objects.equals(startTime, other.startTime) && Objects.equals(endTime, other.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, workPeriodId, activityTypeId, workDate, durationMinutes,
                status, startTime, endTime, breakMinutes);
    }

    @Override
    public String toString() {
        return "WorkEntry[id=" + id + ", userId=" + userId + ", workPeriodId=" + workPeriodId
                + ", activityTypeId=" + activityTypeId + ", workDate=" + workDate
                + ", durationMinutes=" + durationMinutes + ", status=" + status
                + ", startTime=" + startTime + ", endTime=" + endTime
                + ", breakMinutes=" + breakMinutes + "]";
    }
}
