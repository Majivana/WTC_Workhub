package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.model.WorkPeriod;
import co.za.millenniumsolutions.repository.NotificationRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import co.za.millenniumsolutions.repository.WorkPeriodRepository;
import co.za.millenniumsolutions.service.ProgressService;
import co.za.millenniumsolutions.service.WeeklyReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeeklyReminderServiceTests {
    private static final LocalDate START = LocalDate.of(2026, 7, 1);
    private static final WorkPeriod PERIOD = new WorkPeriod("period-1", "July period", START,
            START.plusDays(34), new BigDecimal("18"));

    @TempDir Path tempDir;
    private JdbcTemplate jdbc;
    private UserRepository users;
    private NotificationRepository notifications;
    private WeeklyReminderService service;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:sqlite:" + tempDir.resolve("reminders.db").toAbsolutePath());
        jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE work_period (id TEXT PRIMARY KEY, name TEXT, start_date TEXT, end_date TEXT, weekly_hours_target REAL)");
        jdbc.execute("CREATE TABLE app_user (id TEXT PRIMARY KEY, username TEXT, display_name TEXT, system_role TEXT, work_role_id TEXT, institution_id TEXT, campus_id TEXT, mentor_id TEXT, supervisor_id TEXT, active INTEGER)");
        jdbc.execute("CREATE TABLE work_entry (id TEXT PRIMARY KEY, user_id TEXT, work_period_id TEXT, activity_type_id TEXT, work_date TEXT, duration_minutes INTEGER, status TEXT)");
        jdbc.execute("CREATE TABLE notification (id TEXT PRIMARY KEY, recipient_id TEXT, type TEXT, title TEXT, message TEXT, entity_type TEXT, entity_id TEXT, read_at TEXT, created_at TEXT)");
        jdbc.execute("CREATE UNIQUE INDEX ux_weekly_progress_notification ON notification(recipient_id, type, entity_id) WHERE type = 'WEEKLY_PROGRESS_REMINDER' AND entity_id IS NOT NULL");

        WorkPeriodRepository periods = new WorkPeriodRepository(jdbc);
        periods.save(PERIOD);
        users = new UserRepository(jdbc);
        users.save(student("student-behind"));
        users.save(student("student-on-target"));
        users.save(student("student-ahead"));
        addEntry("student-behind", 17 * 60);
        addEntry("student-on-target", 36 * 60);
        addEntry("student-ahead", 40 * 60);
        notifications = new NotificationRepository(jdbc);
        service = new WeeklyReminderService(periods, users,
                new ProgressService(periods, new WorkEntryRepository(jdbc)), notifications);
    }

    @Test
    void onlyNotifiesStudentsBelowCumulativeExpectedProgress() {
        var result = service.run(START.plusDays(14));

        assertEquals(3, result.studentsEvaluated());
        assertEquals(1, result.studentsBehind());
        assertEquals(1, result.notificationsCreated());
        var created = notifications.findByRecipient("student-behind");
        assertEquals(1, created.size());
        assertEquals("WEEKLY_PROGRESS_REMINDER", created.getFirst().type());
        assertEquals("WORK_PERIOD_WEEK", created.getFirst().entityType());
        assertEquals("period-1:week-2", created.getFirst().entityId());
        assertEquals(List.of(), notifications.findByRecipient("student-on-target"));
        assertEquals(List.of(), notifications.findByRecipient("student-ahead"));
    }

    @Test
    void skipsTheFirstPartialWeekAndDoesNotRemindEveryone() {
        var result = service.run(START.plusDays(6));

        assertEquals(0, result.studentsEvaluated());
        assertEquals(0, result.notificationsCreated());
        assertEquals(List.of(), notifications.findByRecipient("student-behind"));
    }

    @Test
    void repeatedDeliveryIsIdempotentAndEachLaterWeekHasItsOwnReminder() {
        var first = service.run(START.plusDays(14));
        var retry = service.run(START.plusDays(14));

        assertEquals(1, first.notificationsCreated());
        assertEquals(0, retry.notificationsCreated());
        assertEquals(1, notifications.findByRecipient("student-behind").size());

        service.run(START.plusDays(21));

        assertEquals(2, notifications.findByRecipient("student-behind").size());
        assertEquals("period-1:week-3", notifications.findByRecipient("student-behind").getFirst().entityId());
    }

    private void addEntry(String userId, int minutes) {
        jdbc.update("INSERT INTO work_entry(id,user_id,work_period_id,activity_type_id,work_date,duration_minutes,status) VALUES (?,?,?,?,?,?,?)",
                "entry-" + userId, userId, PERIOD.id(), "activity-1", START.toString(), minutes, "DRAFT");
    }

    private static User student(String id) {
        return new User(id, id, id, "STUDENT", "role-1", "campus-1", true);
    }
}
