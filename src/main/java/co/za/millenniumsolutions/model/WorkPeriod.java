package co.za.millenniumsolutions.model;

import java.time.LocalDate;
import java.math.BigDecimal;

public record WorkPeriod(String id, String name, LocalDate startDate, LocalDate endDate,
                         BigDecimal weeklyHoursTarget) {}
