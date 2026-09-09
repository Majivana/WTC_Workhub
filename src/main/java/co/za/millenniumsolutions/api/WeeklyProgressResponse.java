package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.ProgressStatus;

import java.math.BigDecimal;

public record WeeklyProgressResponse(
        BigDecimal targetHours,
        BigDecimal loggedHours,
        BigDecimal verifiedHours,
        BigDecimal pendingHours,
        BigDecimal remainingHours,
        BigDecimal percentage,
        ProgressStatus status) {
}
