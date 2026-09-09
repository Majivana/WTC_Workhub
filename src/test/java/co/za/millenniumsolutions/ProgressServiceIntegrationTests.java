package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.ActivityTypeRepository;
import co.za.millenniumsolutions.repository.InstitutionRepository;
import co.za.millenniumsolutions.repository.CampusRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import co.za.millenniumsolutions.repository.WorkPeriodRepository;
import co.za.millenniumsolutions.service.ProgressService;
import co.za.millenniumsolutions.service.ProgressStatus;
import co.za.millenniumsolutions.service.WeeklyProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProgressServiceIntegrationTests {

    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired UserRepository users;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkPeriodRepository periods;
    @Autowired WorkEntryRepository entries;
    @Autowired ProgressService progressService;

    @BeforeEach
    void setUp() {
        jdbc.execute("DELETE FROM audit_log");
        jdbc.execute("DELETE FROM verification");
        jdbc.execute("DELETE FROM submission");
        jdbc.execute("DELETE FROM evidence_version");
        jdbc.execute("DELETE FROM evidence");
        jdbc.execute("DELETE FROM attendance_capture");
        jdbc.execute("DELETE FROM private_object_reference");
        jdbc.execute("DELETE FROM attendance_session");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus_geofence");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM work_role");
        jdbc.execute("DELETE FROM institution");

        institutions.save(new Institution("i-progress", "Progress Institution"));
        campuses.save(new Campus("c-progress", "i-progress", "Progress Campus"));
        users.save(new User("u-progress", "progress.user", "Progress User", "STUDENT",
                null, "c-progress", true));
        activities.save(new ActivityType("a-progress", "Progress Activity", true));
        periods.save(new WorkPeriod("p-progress", "Configurable Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                new BigDecimal("10")));
    }

    @Test
    void calculatesConfiguredTargetLoggedVerifiedPendingRemainingPercentageAndStatus() {
        saveEntry("verified", LocalDate.of(2026, 9, 10), 120, "VERIFIED");
        saveEntry("pending", LocalDate.of(2026, 9, 11), 60, "SUBMITTED");

        WeeklyProgress progress = progressService.calculate("u-progress", "p-progress");

        assertThat(progress.targetHours()).isEqualByComparingTo("10");
        assertThat(progress.loggedHours()).isEqualByComparingTo("3");
        assertThat(progress.verifiedHours()).isEqualByComparingTo("2");
        assertThat(progress.pendingHours()).isEqualByComparingTo("1");
        assertThat(progress.remainingHours()).isEqualByComparingTo("7");
        assertThat(progress.percentage()).isEqualByComparingTo("30");
        assertThat(progress.status()).isEqualTo(ProgressStatus.BELOW_TARGET);
    }

    @Test
    void excludesEntriesOutsideInclusivePeriodBoundaries() {
        saveEntry("before", LocalDate.of(2026, 8, 31), 600, "VERIFIED");
        saveEntry("start", LocalDate.of(2026, 9, 1), 60, "VERIFIED");
        saveEntry("end", LocalDate.of(2026, 9, 30), 60, "SUBMITTED");
        saveEntry("after", LocalDate.of(2026, 10, 1), 600, "VERIFIED");

        WeeklyProgress progress = progressService.calculate("u-progress", "p-progress");

        assertThat(progress.loggedHours()).isEqualByComparingTo("2");
        assertThat(progress.verifiedHours()).isEqualByComparingTo("1");
    }

    @Test
    void reportsOnTargetAtExactBoundary() {
        saveEntry("exact", LocalDate.of(2026, 9, 15), 600, "VERIFIED");

        WeeklyProgress progress = progressService.calculate("u-progress", "p-progress");

        assertThat(progress.remainingHours()).isZero();
        assertThat(progress.percentage()).isEqualByComparingTo("100");
        assertThat(progress.status()).isEqualTo(ProgressStatus.ON_TARGET);
    }

    @Test
    void reportsOverTargetWithoutClampingPercentage() {
        saveEntry("over-one", LocalDate.of(2026, 9, 15), 600, "VERIFIED");
        saveEntry("over-two", LocalDate.of(2026, 9, 16), 60, "VERIFIED");

        WeeklyProgress progress = progressService.calculate("u-progress", "p-progress");

        assertThat(progress.remainingHours()).isZero();
        assertThat(progress.percentage()).isEqualByComparingTo("110");
        assertThat(progress.status()).isEqualTo(ProgressStatus.OVER_TARGET);
    }

    private void saveEntry(String id, LocalDate date, int minutes, String status) {
        entries.save(new WorkEntry(id, "u-progress", "p-progress", "a-progress",
                date, minutes, status));
    }
}
