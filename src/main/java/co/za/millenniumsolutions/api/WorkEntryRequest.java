package co.za.millenniumsolutions.api;

import java.time.LocalDate;
import java.time.LocalTime;

public record WorkEntryRequest(
        String activityTypeId,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer breakMinutes) {
}
