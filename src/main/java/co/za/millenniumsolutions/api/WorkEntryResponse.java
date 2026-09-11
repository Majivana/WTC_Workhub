package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.WorkEntry;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public final class WorkEntryResponse {
    private final String id;
    private final String userId;
    private final String workPeriodId;
    private final String activityTypeId;
    private final LocalDate workDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int breakMinutes;
    private final int durationMinutes;
    private final String status;

    public WorkEntryResponse(String id, String userId, String workPeriodId, String activityTypeId,
                             LocalDate workDate, LocalTime startTime, LocalTime endTime,
                             int breakMinutes, int durationMinutes, String status) {
        this.id = id;
        this.userId = userId;
        this.workPeriodId = workPeriodId;
        this.activityTypeId = activityTypeId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

    public static WorkEntryResponse from(WorkEntry entry) {
        return new WorkEntryResponse(entry.id(), entry.userId(), entry.workPeriodId(),
                entry.activityTypeId(), entry.workDate(), entry.startTime(), entry.endTime(),
                entry.breakMinutes(), entry.durationMinutes(), entry.status());
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
    public LocalTime startTime() { return startTime; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime endTime() { return endTime; }
    public LocalTime getEndTime() { return endTime; }
    public int breakMinutes() { return breakMinutes; }
    public int getBreakMinutes() { return breakMinutes; }
    public int durationMinutes() { return durationMinutes; }
    public int getDurationMinutes() { return durationMinutes; }
    public String status() { return status; }
    public String getStatus() { return status; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WorkEntryResponse other)) return false;
        return breakMinutes == other.breakMinutes && durationMinutes == other.durationMinutes
                && Objects.equals(id, other.id) && Objects.equals(userId, other.userId)
                && Objects.equals(workPeriodId, other.workPeriodId) && Objects.equals(activityTypeId, other.activityTypeId)
                && Objects.equals(workDate, other.workDate) && Objects.equals(startTime, other.startTime)
                && Objects.equals(endTime, other.endTime) && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() { return Objects.hash(id, userId, workPeriodId, activityTypeId, workDate, startTime, endTime, breakMinutes, durationMinutes, status); }

    @Override
    public String toString() {
        return "WorkEntryResponse[id=" + id + ", userId=" + userId + ", workPeriodId=" + workPeriodId
                + ", activityTypeId=" + activityTypeId + ", workDate=" + workDate + ", startTime=" + startTime
                + ", endTime=" + endTime + ", breakMinutes=" + breakMinutes + ", durationMinutes=" + durationMinutes
                + ", status=" + status + "]";
    }
}
