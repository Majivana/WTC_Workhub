package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.ActivityType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ActivityTypeRepository extends RepositorySupport {
    public ActivityTypeRepository(JdbcTemplate j) {
        super(j);
    }

    public ActivityType save(ActivityType x) {
        update("""
                INSERT INTO activity_type(id,name,active) VALUES (?,?,?)
                ON CONFLICT(id) DO UPDATE SET name=excluded.name,active=excluded.active
                """, x.id(), x.name(), x.active() ? 1 : 0);
        return x;
    }

    public List<ActivityType> findAll(boolean includeInactive) {
        return jdbc.query("SELECT id,name,active FROM activity_type " +
                        (includeInactive ? "" : "WHERE active = 1 ") + "ORDER BY name",
                (rs, rowNum) -> new ActivityType(rs.getString("id"), rs.getString("name"),
                        rs.getInt("active") == 1));
    }

    public Optional<ActivityType> findById(String id) {
        return jdbc.query("SELECT id,name,active FROM activity_type WHERE id = ?",
                        (rs, rowNum) -> new ActivityType(rs.getString("id"), rs.getString("name"),
                                rs.getInt("active") == 1), id)
                .stream().findFirst();
    }

    public boolean existsByName(String name, String excludedId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM activity_type WHERE name = ? AND id <> ?",
                Integer.class, name, excludedId) > 0;
    }

    public void deactivateById(String id) {
        update("UPDATE activity_type SET active = 0 WHERE id = ?", id);
    }
}
