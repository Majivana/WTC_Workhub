package co.za.millenniumsolutions.model;

import java.time.LocalDate;
import java.math.BigDecimal;

import java.util.Objects;

public final class WorkPeriod {
    private final String id;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal weeklyHoursTarget;

    public WorkPeriod(String id, String name, LocalDate startDate, LocalDate endDate, BigDecimal weeklyHoursTarget) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weeklyHoursTarget = weeklyHoursTarget;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String name() { return name; }
    public String getName() { return name; }

    public LocalDate startDate() { return startDate; }
    public LocalDate getStartDate() { return startDate; }

    public LocalDate endDate() { return endDate; }
    public LocalDate getEndDate() { return endDate; }

    public BigDecimal weeklyHoursTarget() { return weeklyHoursTarget; }
    public BigDecimal getWeeklyHoursTarget() { return weeklyHoursTarget; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof WorkPeriod other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name) && Objects.equals(startDate, other.startDate) && Objects.equals(endDate, other.endDate) && Objects.equals(weeklyHoursTarget, other.weeklyHoursTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, startDate, endDate, weeklyHoursTarget);
    }

    @Override
    public String toString() {
        return "WorkPeriod[id=" + id + ", name=" + name + ", startDate=" + startDate + ", endDate=" + endDate + ", weeklyHoursTarget=" + weeklyHoursTarget + "]";
    }
}
