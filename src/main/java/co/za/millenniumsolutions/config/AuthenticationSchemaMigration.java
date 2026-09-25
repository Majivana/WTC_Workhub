package co.za.millenniumsolutions.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSchemaMigration {
    public AuthenticationSchemaMigration(JdbcTemplate jdbc) {
        Integer passwordColumns = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('app_user') WHERE name = 'password_hash'",
                Integer.class);
        if (passwordColumns != null && passwordColumns == 0) {
            jdbc.execute("ALTER TABLE app_user ADD COLUMN password_hash TEXT");
        }
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS auth_session (
                    id TEXT PRIMARY KEY, user_id TEXT NOT NULL, token_hash TEXT NOT NULL UNIQUE,
                    expires_at TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    revoked_at TEXT, FOREIGN KEY (user_id) REFERENCES app_user(id)
                )
                """);
        jdbc.execute("CREATE INDEX IF NOT EXISTS ix_auth_session_token ON auth_session(token_hash)");
        // Sessions are intentionally invalidated on application restart; this also removes
        // sessions belonging to development users that were recreated during local resets.
        jdbc.execute("DELETE FROM auth_session");
    }
}
