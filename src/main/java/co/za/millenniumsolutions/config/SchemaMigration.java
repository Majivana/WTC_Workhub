package co.za.millenniumsolutions.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigration {
    public SchemaMigration(JdbcTemplate jdbc) {
        Integer changeNotesColumns = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('evidence_version') WHERE name = 'change_notes'",
                Integer.class);
        if (changeNotesColumns != null && changeNotesColumns == 0) {
            jdbc.execute("ALTER TABLE evidence_version ADD COLUMN change_notes TEXT");
        }
    }
}
