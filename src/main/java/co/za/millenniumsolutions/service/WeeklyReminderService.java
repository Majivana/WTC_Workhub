package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.Notification;
import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.model.WorkPeriod;
import co.za.millenniumsolutions.repository.NotificationRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.repository.WorkPeriodRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class WeeklyReminderService {
    public static final String NOTIFICATION_TYPE = "WEEKLY_PROGRESS_REMINDER";

    private final WorkPeriodRepository workPeriods;
    private final UserRepository users;
    private final ProgressService progress;
    private final NotificationRepository notifications;

    public WeeklyReminderService(WorkPeriodRepository workPeriods, UserRepository users,
                                 ProgressService progress, NotificationRepository notifications) {
        this.workPeriods = workPeriods;
        this.users = users;
        this.progress = progress;
        this.notifications = notifications;
    }

    /** Evaluates cumulative progress at completed seven-day boundaries from each period's start. */
    public ReminderRunResult run(LocalDate evaluationDate) {
        int evaluated = 0;
        int behind = 0;
        int created = 0;

        for (WorkPeriod period : workPeriods.findActiveOn(evaluationDate)) {
            long completedWeeks = ChronoUnit.DAYS.between(period.startDate(), evaluationDate) / 7;
            if (completedWeeks < 1 || period.weeklyHoursTarget().signum() <= 0) continue;

            BigDecimal expectedHours = period.weeklyHoursTarget().multiply(BigDecimal.valueOf(completedWeeks));
            for (User student : users.findActiveStudents()) {
                evaluated++;
                WeeklyProgress current = progress.calculate(student.id(), period.id());
                if (current.loggedHours().compareTo(expectedHours) >= 0) continue;

                behind++;
                String reminderKey = period.id() + ":week-" + completedWeeks;
                Notification reminder = new Notification(UUID.randomUUID().toString(), student.id(),
                        NOTIFICATION_TYPE, "Weekly progress reminder",
                        "Your recorded work hours are below the expected progress for this work period. " +
                                "Please review your dashboard.",
                        "WORK_PERIOD_WEEK", reminderKey, null, Instant.now());
                if (notifications.saveIfAbsent(reminder)) created++;
            }
        }
        return new ReminderRunResult(evaluated, behind, created);
    }

    public record ReminderRunResult(int studentsEvaluated, int studentsBehind, int notificationsCreated) {}
}
