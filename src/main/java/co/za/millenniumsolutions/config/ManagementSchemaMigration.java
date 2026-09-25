package co.za.millenniumsolutions.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ManagementSchemaMigration {
    public ManagementSchemaMigration(JdbcTemplate jdbc) {
        addColumnIfMissing(jdbc, "institution_id TEXT REFERENCES institution(id)");
        addColumnIfMissing(jdbc, "mentor_id TEXT REFERENCES app_user(id)");
        addColumnIfMissing(jdbc, "supervisor_id TEXT REFERENCES app_user(id)");
    }

    private void addColumnIfMissing(JdbcTemplate jdbc, String definition) {
        String name = definition.substring(0, definition.indexOf(' '));
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('app_user') WHERE name = ?", Integer.class, name);
        if (count != null && count == 0) {
            jdbc.execute("ALTER TABLE app_user ADD COLUMN " + definition);
        }
    }
}
