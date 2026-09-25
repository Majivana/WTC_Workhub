package co.za.millenniumsolutions.config;

import co.za.millenniumsolutions.security.Permission;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionSchemaMigration {
    public RolePermissionSchemaMigration(JdbcTemplate jdbc) {
        for (Permission permission : Permission.values()) {
            jdbc.update("INSERT OR IGNORE INTO permission(code,description) VALUES (?,?)",
                    permission.name(), permission.name().replace('_', ' '));
        }
        grant(jdbc, "STUDENT", Permission.WORK_ENTRY_READ, Permission.WORK_ENTRY_MANAGE);
        grant(jdbc, "SUPERVISOR", Permission.USER_READ, Permission.ORGANIZATION_READ,
                Permission.SUBMISSION_REVIEW, Permission.VERIFICATION_REVIEW, Permission.REPORT_READ);
        grant(jdbc, "MENTOR", Permission.USER_READ, Permission.SUBMISSION_REVIEW,
                Permission.VERIFICATION_REVIEW, Permission.REPORT_READ);
        grant(jdbc, "ADMIN", Permission.values());
        grant(jdbc, "SUPER_ADMIN", Permission.values());
    }

    private void grant(JdbcTemplate jdbc, String role, Permission... permissions) {
        for (Permission permission : permissions) {
            jdbc.update("INSERT OR IGNORE INTO role_permission(system_role,permission_code) VALUES (?,?)",
                    role, permission.name());
        }
    }
}
