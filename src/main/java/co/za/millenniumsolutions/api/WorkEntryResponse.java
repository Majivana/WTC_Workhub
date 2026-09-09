package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.WorkEntry;

import java.time.LocalDate;
import java.time.LocalTime;

public record WorkEntryResponse(
        String id,
        String userId,
        String workPeriodId,
        String activityTypeId,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        int breakMinutes,
        int durationMinutes,
        String status) {
    public static WorkEntryResponse from(WorkEntry entry) {
        return new WorkEntryResponse(entry.id(), entry.userId(), entry.workPeriodId(),
                entry.activityTypeId(), entry.workDate(), entry.startTime(), entry.endTime(),
                entry.breakMinutes(), entry.durationMinutes(), entry.status());
    }
}
