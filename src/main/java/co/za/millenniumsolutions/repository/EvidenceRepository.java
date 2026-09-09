package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.Evidence; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class EvidenceRepository extends RepositorySupport { public EvidenceRepository(JdbcTemplate j){super(j);} public Evidence save(Evidence x){update("INSERT INTO evidence(id,work_entry_id,status) VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET status=excluded.status",x.id(),x.workEntryId(),x.status()); return x;} }
