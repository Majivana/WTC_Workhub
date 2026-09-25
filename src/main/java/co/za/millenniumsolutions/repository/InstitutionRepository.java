package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.Institution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository public class InstitutionRepository extends RepositorySupport {
 public InstitutionRepository(JdbcTemplate jdbc){super(jdbc);}
 public Institution save(Institution x){update("INSERT INTO institution(id,name) VALUES (?,?) ON CONFLICT(id) DO UPDATE SET name=excluded.name",x.id(),x.name()); return x;}
 public Optional<Institution> findById(String id){return jdbc.query("SELECT id,name FROM institution WHERE id=?", (rs,n)->new Institution(rs.getString("id"),rs.getString("name")),id).stream().findFirst();}
 public List<Institution> findAll(){return jdbc.query("SELECT id,name FROM institution ORDER BY name",(rs,n)->new Institution(rs.getString("id"),rs.getString("name")));}
}
