package co.za.millenniumsolutions.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record WorkEntry(String id, String userId, String workPeriodId, String activityTypeId,
                        LocalDate workDate, int durationMinutes, String status,
                        LocalTime startTime, LocalTime endTime, int breakMinutes) {
    public WorkEntry(String id, String userId, String workPeriodId, String activityTypeId,
                     LocalDate workDate, int durationMinutes, String status) {
        this(id, userId, workPeriodId, activityTypeId, workDate, durationMinutes, status,
                null, null, 0);
    }
}
