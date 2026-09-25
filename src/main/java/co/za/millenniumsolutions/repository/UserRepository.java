package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.model.AuthUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository extends RepositorySupport {
    public UserRepository(JdbcTemplate j) {
        super(j);
    }

    public User save(User x) {
        update("INSERT INTO app_user(id,username,display_name,system_role,work_role_id,institution_id,campus_id,mentor_id,supervisor_id,active) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET username=excluded.username," +
                        "display_name=excluded.display_name,system_role=excluded.system_role," +
                        "work_role_id=excluded.work_role_id,institution_id=excluded.institution_id," +
                        "campus_id=excluded.campus_id,mentor_id=excluded.mentor_id," +
                        "supervisor_id=excluded.supervisor_id,active=excluded.active",
                x.id(), x.username(), x.displayName(), x.systemRole(), x.workRoleId(), x.institutionId(),
                x.campusId(), x.mentorId(), x.supervisorId(), x.active() ? 1 : 0);
        return x;
    }

    public Optional<User> findById(String id) {
        return jdbc.query("SELECT id,username,display_name,system_role,work_role_id,institution_id,campus_id,mentor_id,supervisor_id,active " +
                        "FROM app_user WHERE id = ?",
                (rs, rowNum) -> new User(rs.getString("id"), rs.getString("username"),
                        rs.getString("display_name"), rs.getString("system_role"),
                        rs.getString("work_role_id"), rs.getString("institution_id"), rs.getString("campus_id"),
                        rs.getString("mentor_id"), rs.getString("supervisor_id"),
                        rs.getInt("active") == 1), id).stream().findFirst();
    }

    public java.util.List<User> findAll(boolean includeInactive) {
        String sql = "SELECT id,username,display_name,system_role,work_role_id,institution_id,campus_id,mentor_id,supervisor_id,active FROM app_user"
                + (includeInactive ? "" : " WHERE active = 1") + " ORDER BY username";
        return jdbc.query(sql, (rs, rowNum) -> new User(rs.getString("id"), rs.getString("username"),
                rs.getString("display_name"), rs.getString("system_role"), rs.getString("work_role_id"),
                rs.getString("institution_id"), rs.getString("campus_id"), rs.getString("mentor_id"),
                rs.getString("supervisor_id"), rs.getInt("active") == 1));
    }

    public java.util.List<User> findActiveStudents() {
        return jdbc.query("SELECT id,username,display_name,system_role,work_role_id,institution_id,campus_id,mentor_id,supervisor_id,active " +
                        "FROM app_user WHERE active = 1 AND system_role = 'STUDENT' ORDER BY id",
                (rs, rowNum) -> new User(rs.getString("id"), rs.getString("username"),
                        rs.getString("display_name"), rs.getString("system_role"),
                        rs.getString("work_role_id"), rs.getString("institution_id"), rs.getString("campus_id"),
                        rs.getString("mentor_id"), rs.getString("supervisor_id"), true));
    }

    public void deactivateById(String id) {
        update("UPDATE app_user SET active = 0 WHERE id = ?", id);
    }

    public Optional<AuthUser> findAuthByUsername(String username) {
        return jdbc.query("SELECT id,username,password_hash,system_role,active FROM app_user WHERE username = ?",
                        (rs, rowNum) -> new AuthUser(rs.getString("id"), rs.getString("username"),
                                rs.getString("password_hash"), rs.getString("system_role"),
                                rs.getInt("active") == 1), username).stream().findFirst();
    }

    public Optional<AuthUser> findAuthById(String id) {
        return jdbc.query("SELECT id,username,password_hash,system_role,active FROM app_user WHERE id = ?",
                (rs, rowNum) -> new AuthUser(rs.getString("id"), rs.getString("username"),
                        rs.getString("password_hash"), rs.getString("system_role"),
                        rs.getInt("active") == 1), id).stream().findFirst();
    }

    public void setPasswordHash(String userId, String passwordHash) {
        update("UPDATE app_user SET password_hash = ? WHERE id = ?", passwordHash, userId);
    }
}
