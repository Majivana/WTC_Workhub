package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.WorkEntrySummary;
import co.za.millenniumsolutions.model.WorkPeriod;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import co.za.millenniumsolutions.repository.WorkPeriodRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProgressService {

    private static final BigDecimal MINUTES_PER_HOUR = BigDecimal.valueOf(60);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final WorkPeriodRepository workPeriods;
    private final WorkEntryRepository workEntries;

    public ProgressService(WorkPeriodRepository workPeriods, WorkEntryRepository workEntries) {
        this.workPeriods = workPeriods;
        this.workEntries = workEntries;
    }

    public WeeklyProgress calculate(String userId, String workPeriodId) {
        WorkPeriod period = workPeriods.findById(workPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown work period: " + workPeriodId));
        WorkEntrySummary summary = workEntries.summarize(userId, period);

        BigDecimal target = period.weeklyHoursTarget().stripTrailingZeros();
        BigDecimal logged = hours(summary.loggedMinutes());
        BigDecimal verified = hours(summary.verifiedMinutes());
        BigDecimal pending = logged.subtract(verified).max(BigDecimal.ZERO);
        BigDecimal remaining = target.subtract(logged).max(BigDecimal.ZERO);
        BigDecimal percentage = target.signum() == 0
                ? BigDecimal.ZERO
                : logged.multiply(ONE_HUNDRED).divide(target, 2, RoundingMode.HALF_UP);

        ProgressStatus status = logged.compareTo(target) < 0
                ? ProgressStatus.BELOW_TARGET
                : logged.compareTo(target) == 0 ? ProgressStatus.ON_TARGET : ProgressStatus.OVER_TARGET;

        return new WeeklyProgress(target, logged, verified, pending, remaining, percentage, status);
    }

    private BigDecimal hours(long minutes) {
        return BigDecimal.valueOf(minutes)
                .divide(MINUTES_PER_HOUR, 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }
}
