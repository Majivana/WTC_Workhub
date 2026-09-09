package co.za.millenniumsolutions.repository;
import co.za.millenniumsolutions.model.Submission; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Repository;
@Repository public class SubmissionRepository extends RepositorySupport { public SubmissionRepository(JdbcTemplate j){super(j);} public Submission save(Submission x){update("INSERT INTO submission(id,work_entry_id,status) VALUES (?,?,?) ON CONFLICT(id) DO UPDATE SET status=excluded.status",x.id(),x.workEntryId(),x.status()); return x;} }
