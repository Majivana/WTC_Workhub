package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.Campus; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public class CampusRepository extends RepositorySupport {
 public CampusRepository(JdbcTemplate j){super(j);}
 public Campus save(Campus x){update("INSERT INTO campus(id,institution_id,name) VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET institution_id=excluded.institution_id,name=excluded.name",x.id(),x.institutionId(),x.name()); return x;}
 public Optional<Campus> findById(String id){return jdbc.query("SELECT id,institution_id,name FROM campus WHERE id=?", (rs,n)->new Campus(rs.getString("id"),rs.getString("institution_id"),rs.getString("name")),id).stream().findFirst();}
 public List<Campus> findAll(){return jdbc.query("SELECT id,institution_id,name FROM campus ORDER BY name",(rs,n)->new Campus(rs.getString("id"),rs.getString("institution_id"),rs.getString("name")));}
}
