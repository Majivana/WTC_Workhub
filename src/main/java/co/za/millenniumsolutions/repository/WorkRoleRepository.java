package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.WorkRole; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public class WorkRoleRepository extends RepositorySupport {
 public WorkRoleRepository(JdbcTemplate j){super(j);}
 public WorkRole save(WorkRole x){update("INSERT INTO work_role(id,code,name) VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET code=excluded.code,name=excluded.name",x.id(),x.code(),x.name()); return x;}
 public Optional<WorkRole> findById(String id){return jdbc.query("SELECT id,code,name FROM work_role WHERE id=?", (rs,n)->new WorkRole(rs.getString("id"),rs.getString("code"),rs.getString("name")),id).stream().findFirst();}
 public List<WorkRole> findAll(){return jdbc.query("SELECT id,code,name FROM work_role ORDER BY name",(rs,n)->new WorkRole(rs.getString("id"),rs.getString("code"),rs.getString("name")));}
}
