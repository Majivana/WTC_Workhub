package co.za.millenniumsolutions.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Loads deterministic local demonstration data only when explicitly enabled.
 */
@Component
@Profile("local")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class LocalSeedData {

    public LocalSeedData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("""
                INSERT OR IGNORE INTO institution (id, name)
                VALUES ('institution-wtc', 'WeThinkCode_')
                """);
        jdbcTemplate.update("""
                INSERT OR IGNORE INTO campus
                    (id, institution_id, name, latitude, longitude, geofence_radius_metres)
                VALUES
                    ('campus-cape-town', 'institution-wtc', 'Cape Town', -33.9249, 18.4241, 150)
                """);
        jdbcTemplate.update("""
                INSERT OR IGNORE INTO work_role (id, code, name)
                VALUES ('role-peer-tutor', 'PEER_TUTOR', 'Peer Tutor')
                """);
        jdbcTemplate.update("""
                INSERT OR IGNORE INTO app_user
                    (id, username, display_name, system_role, work_role_id, campus_id)
                VALUES
                    ('user-student-demo', 'student.demo', 'Demo Student', 'STUDENT',
                     'role-peer-tutor', 'campus-cape-town'),
                    ('user-supervisor-demo', 'supervisor.demo', 'Demo Supervisor', 'SUPERVISOR',
                     NULL, 'campus-cape-town')
                """);
        jdbcTemplate.update("""
                INSERT OR IGNORE INTO work_period
                    (id, name, start_date, end_date, weekly_hours_target)
                VALUES
                    ('period-july-august-2026', 'July-August 2026',
                     '2026-07-21', '2026-08-20', 18)
                """);
        jdbcTemplate.update("""
                INSERT OR IGNORE INTO activity_type (id, name)
                VALUES
                    ('activity-student-support', 'Student Support'),
                    ('activity-code-review', 'Code Review'),
                    ('activity-workshop', 'Workshop')
                """);
    }
}
