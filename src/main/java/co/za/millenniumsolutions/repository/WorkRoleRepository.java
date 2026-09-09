package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.WorkRole; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class WorkRoleRepository extends RepositorySupport { public WorkRoleRepository(JdbcTemplate j){super(j);} public WorkRole save(WorkRole x){update("INSERT INTO work_role(id,code,name) VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET code=excluded.code,name=excluded.name",x.id(),x.code(),x.name()); return x;} }
