package co.za.millenniumsolutions.model;

import java.time.LocalTime;

import java.util.Objects;

public final class WorkEntryTiming {
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int breakMinutes;

    public WorkEntryTiming(LocalTime startTime, LocalTime endTime, int breakMinutes) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakMinutes = breakMinutes;
    }

    public LocalTime startTime() { return startTime; }
    public LocalTime getStartTime() { return startTime; }

    public LocalTime endTime() { return endTime; }
    public LocalTime getEndTime() { return endTime; }

    public int breakMinutes() { return breakMinutes; }
    public int getBreakMinutes() { return breakMinutes; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WorkEntryTiming other)) return false;
        return Objects.equals(startTime, other.startTime) && Objects.equals(endTime, other.endTime) && breakMinutes == other.breakMinutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, breakMinutes);
    }

    @Override
    public String toString() {
        return "WorkEntryTiming[startTime=" + startTime + ", endTime=" + endTime + ", breakMinutes=" + breakMinutes + "]";
    }
}
