package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository extends RepositorySupport {
    public UserRepository(JdbcTemplate j) {
        super(j);
    }

    public User save(User x) {
        update("INSERT INTO app_user(id,username,display_name,system_role,work_role_id,campus_id,active) " +
                        "VALUES (?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET username=excluded.username," +
                        "display_name=excluded.display_name,system_role=excluded.system_role," +
                        "work_role_id=excluded.work_role_id,campus_id=excluded.campus_id,active=excluded.active",
                x.id(), x.username(), x.displayName(), x.systemRole(), x.workRoleId(), x.campusId(),
                x.active() ? 1 : 0);
        return x;
    }

    public Optional<User> findById(String id) {
        return jdbc.query("SELECT id,username,display_name,system_role,work_role_id,campus_id,active " +
                        "FROM app_user WHERE id = ?",
                (rs, rowNum) -> new User(rs.getString("id"), rs.getString("username"),
                        rs.getString("display_name"), rs.getString("system_role"),
                        rs.getString("work_role_id"), rs.getString("campus_id"),
                        rs.getInt("active") == 1), id).stream().findFirst();
    }
}
