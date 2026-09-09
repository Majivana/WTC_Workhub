package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.ActivityType; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class ActivityTypeRepository extends RepositorySupport { public ActivityTypeRepository(JdbcTemplate j){super(j);} public ActivityType save(ActivityType x){update("INSERT INTO activity_type(id,name,active) VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET name=excluded.name,active=excluded.active",x.id(),x.name(),x.active()?1:0); return x;} }
