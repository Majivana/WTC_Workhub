package co.za.millenniumsolutions.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PermissionRepository extends RepositorySupport {
    public PermissionRepository(JdbcTemplate jdbc) { super(jdbc); }

    public List<String> findForRole(String role) {
        return jdbc.queryForList("""
                SELECT permission_code FROM role_permission
                WHERE system_role = ? ORDER BY permission_code
                """, String.class, role);
    }
}
