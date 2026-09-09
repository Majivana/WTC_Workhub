package co.za.millenniumsolutions.model;

import java.time.LocalDate;

public record WorkEntry(String id, String userId, String workPeriodId, String activityTypeId,
                        LocalDate workDate, int durationMinutes, String status) {}
