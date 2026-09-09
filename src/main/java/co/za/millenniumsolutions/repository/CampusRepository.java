package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.Campus; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class CampusRepository extends RepositorySupport { public CampusRepository(JdbcTemplate j){super(j);} public Campus save(Campus x){update("INSERT INTO campus(id,institution_id,name) VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET institution_id=excluded.institution_id,name=excluded.name",x.id(),x.institutionId(),x.name()); return x;} }
