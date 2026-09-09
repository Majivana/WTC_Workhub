package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.Institution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository public class InstitutionRepository extends RepositorySupport {
 public InstitutionRepository(JdbcTemplate jdbc){super(jdbc);}
 public Institution save(Institution x){update("INSERT INTO institution(id,name) VALUES (?,?) ON CONFLICT(id) DO UPDATE SET name=excluded.name",x.id(),x.name()); return x;}
}
