package co.za.millenniumsolutions.service;

import java.math.BigDecimal;

public record WeeklyProgress(
        BigDecimal targetHours,
        BigDecimal loggedHours,
        BigDecimal verifiedHours,
        BigDecimal pendingHours,
        BigDecimal remainingHours,
        BigDecimal percentage,
        ProgressStatus status) {
}
