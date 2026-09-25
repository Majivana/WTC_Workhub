package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@IsolatedSqliteTest
@SpringBootTest
@ActiveProfiles("integration")
class DatabaseIntegrationProfileTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired WorkRoleRepository roles;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkEntryRepository entries;

    @BeforeEach
    void reset() {
        jdbc.execute("DELETE FROM work_entry_timing");
        jdbc.execute("DELETE FROM work_entry");
        jdbc.execute("DELETE FROM app_user");
        jdbc.execute("DELETE FROM activity_type");
        jdbc.execute("DELETE FROM work_period");
        jdbc.execute("DELETE FROM campus");
        jdbc.execute("DELETE FROM work_role");
        jdbc.execute("DELETE FROM institution");
    }

    @Test
    void repositoryMappingsRoundTripRelationships() {
        Institution institution = institutions.save(new Institution("db-i", "Database Institution"));
        Campus campus = campuses.save(new Campus("db-c", institution.id(), "Database Campus"));
        roles.save(new WorkRole("db-r", "DB_ROLE", "Database Role"));
        User user = users.save(new User("db-u", "db.user", "Database User", "STUDENT",
                "db-r", institution.id(), campus.id(), null, null, true));
        WorkPeriod period = periods.save(new WorkPeriod("db-p", "Database Period",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), new BigDecimal("18")));
        activities.save(new ActivityType("db-a", "Database Activity", true));
        WorkEntry entry = entries.save(new WorkEntry("db-w", user.id(), period.id(), "db-a",
                LocalDate.of(2026, 9, 10), 90, "DRAFT"));

        assertThat(institutions.findById(institution.id())).contains(institution);
        assertThat(campuses.findById(campus.id())).contains(campus);
        assertThat(users.findById(user.id())).get().extracting(User::institutionId, User::campusId)
                .containsExactly(institution.id(), campus.id());
        assertThat(periods.findById(period.id())).get()
                .extracting(WorkPeriod::id, WorkPeriod::name, WorkPeriod::startDate,
                        WorkPeriod::endDate)
                .containsExactly(period.id(), period.name(), period.startDate(), period.endDate());
        assertThat(periods.findById(period.id()).orElseThrow().weeklyHoursTarget())
                .isEqualByComparingTo(period.weeklyHoursTarget());
        assertThat(entries.findById(entry.id())).get().extracting(WorkEntry::userId, WorkEntry::durationMinutes)
                .containsExactly(user.id(), 90);
    }

    @Test
    void databaseConstraintsRejectBrokenReferencesAndInvalidPeriodRange() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO campus(id,institution_id,name) VALUES ('bad-campus','missing-institution','Bad')
                """)).isInstanceOfAny(DataIntegrityViolationException.class,
                org.springframework.jdbc.UncategorizedSQLException.class);

        assertThatThrownBy(() -> periods.save(new WorkPeriod("bad-period", "Bad Period",
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1), new BigDecimal("18"))))
                .isInstanceOfAny(DataIntegrityViolationException.class,
                        org.springframework.jdbc.UncategorizedSQLException.class);
    }
}
