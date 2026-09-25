package co.za.millenniumsolutions.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class HistorySchemaMigration {
    public HistorySchemaMigration(JdbcTemplate jdbc) {
        Integer commentColumns = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('verification') WHERE name = 'comment'",
                Integer.class);
        if (commentColumns != null && commentColumns == 0) {
            jdbc.execute("ALTER TABLE verification ADD COLUMN comment TEXT");
            jdbc.update("UPDATE verification SET comment = ? WHERE comment IS NULL",
                    "Historical decision imported before verification comments were required");
        }
    }
}
