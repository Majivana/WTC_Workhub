package co.za.millenniumsolutions.api;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.Objects;

public class WorkEntryRequest {
    private String activityTypeId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer breakMinutes;

    public WorkEntryRequest() {
    }

    public WorkEntryRequest(String activityTypeId, LocalDate workDate, LocalTime startTime, LocalTime endTime, Integer breakMinutes) {
        this.activityTypeId = activityTypeId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
    }

    public String activityTypeId() { return activityTypeId; }
    public String getActivityTypeId() { return activityTypeId; }
    public void setActivityTypeId(String activityTypeId) { this.activityTypeId = activityTypeId; }

    public LocalDate workDate() { return workDate; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalTime startTime() { return startTime; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime endTime() { return endTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer breakMinutes() { return breakMinutes; }
    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WorkEntryRequest other)) return false;
        return Objects.equals(activityTypeId, other.activityTypeId) && Objects.equals(workDate, other.workDate) && Objects.equals(startTime, other.startTime) && Objects.equals(endTime, other.endTime) && Objects.equals(breakMinutes, other.breakMinutes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityTypeId, workDate, startTime, endTime, breakMinutes);
    }

    @Override
    public String toString() {
        return "WorkEntryRequest[activityTypeId=" + activityTypeId + ", workDate=" + workDate + ", startTime=" + startTime + ", endTime=" + endTime + ", breakMinutes=" + breakMinutes + "]";
    }
}
