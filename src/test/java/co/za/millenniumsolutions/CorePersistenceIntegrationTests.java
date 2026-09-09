package co.za.millenniumsolutions;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CorePersistenceIntegrationTests {

    @Autowired JdbcTemplate jdbc;
    @Autowired InstitutionRepository institutions;
    @Autowired CampusRepository campuses;
    @Autowired CampusGeofenceRepository geofences;
    @Autowired WorkRoleRepository roles;
    @Autowired UserRepository users;
    @Autowired WorkPeriodRepository periods;
    @Autowired ActivityTypeRepository activities;
    @Autowired WorkEntryRepository entries;
    @Autowired AttendanceSessionRepository sessions;
    @Autowired PrivateObjectReferenceRepository objects;
    @Autowired AttendanceCaptureRepository captures;
    @Autowired EvidenceRepository evidence;
    @Autowired EvidenceVersionRepository versions;

    @BeforeEach
    void clearIntegrationRows() {
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
    }

    @Test
    void persistsCoreRelationshipsAndKeepsPrivateObjectSeparate() {
        institutions.save(new Institution("i-it", "Integration Institution"));
        campuses.save(new Campus("c-it", "i-it", "Integration Campus"));
        geofences.save(new CampusGeofence("g-it", "c-it", new BigDecimal("-33.9"),
                new BigDecimal("18.4"), 125, true));
        roles.save(new WorkRole("r-it", "TEST_ROLE", "Test Role"));
        users.save(new User("u-it", "integration.user", "Integration User", "STUDENT",
                "r-it", "c-it", true));
        periods.save(new WorkPeriod("p-it", "Integration Period", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), new BigDecimal("18")));
        activities.save(new ActivityType("a-it", "Integration Activity", true));
        entries.save(new WorkEntry("w-it", "u-it", "p-it", "a-it",
                LocalDate.of(2026, 9, 9), 60, "DRAFT"));
        sessions.save(new AttendanceSession("s-it", "u-it", "c-it", "p-it",
                Instant.parse("2026-09-09T08:00:00Z"), null, null, "ACTIVE", "recon-it"));
        objects.save(new PrivateObjectReference("o-it", "private/it/selfie", "image/jpeg",
                42, "sha256-it", "ATTENDANCE_SELFIE", "u-it", Instant.now()));
        captures.save(new AttendanceCapture("cap-it", "s-it", "CLOCK_IN",
                Instant.parse("2026-09-09T08:00:01Z"), "o-it", true, "WITHIN_GEOFENCE"));
        evidence.save(new Evidence("e-it", "w-it", "DRAFT"));
        versions.save(new EvidenceVersion("ev-it", "e-it", 1, "o-it", "sha256-it",
                Instant.parse("2026-09-09T08:01:00Z")));

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM attendance_capture ac JOIN private_object_reference por " +
                        "ON por.id = ac.private_object_reference_id WHERE ac.id = 'cap-it'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('attendance_capture') WHERE name = 'selfie_object_key'",
                Integer.class)).isZero();
        assertThat(jdbc.queryForObject(
                "SELECT reconciliation_reference FROM attendance_session WHERE id = 's-it'", String.class))
                .isEqualTo("recon-it");
    }

    @Test
    void enforcesForeignKeysAndOnlyOneActiveSessionPerUser() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO campus_geofence(id,campus_id,latitude,longitude,radius_metres) " +
                        "VALUES ('bad-geofence','missing',0,0,100)"))
                .isInstanceOfAny(DataIntegrityViolationException.class,
                        org.springframework.jdbc.UncategorizedSQLException.class);

        institutions.save(new Institution("i-unique", "Unique Institution"));
        campuses.save(new Campus("c-unique", "i-unique", "Unique Campus"));
        users.save(new User("u-unique", "unique.user", "Unique User", "STUDENT", null, "c-unique", true));
        jdbc.update("INSERT INTO attendance_session(id,user_id,campus_id,status) VALUES (?,?,?,'ACTIVE')",
                "s-unique-1", "u-unique", "c-unique");

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO attendance_session(id,user_id,campus_id,status) VALUES (?,?,?,'ACTIVE')",
                "s-unique-2", "u-unique", "c-unique"))
                .isInstanceOfAny(DataIntegrityViolationException.class,
                        org.springframework.jdbc.UncategorizedSQLException.class);
    }
}
