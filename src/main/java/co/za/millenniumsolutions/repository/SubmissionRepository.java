package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.Submission;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubmissionRepository extends RepositorySupport {
    public SubmissionRepository(JdbcTemplate j) {
        super(j);
    }

    public Submission save(Submission x) {
        update("INSERT INTO submission(id,work_entry_id,status) VALUES (?,?,?) " +
                        "ON CONFLICT(id) DO UPDATE SET status=excluded.status",
                x.id(), x.workEntryId(), x.status());
        return x;
    }

    public Optional<Submission> findById(String id) {
        return jdbc.query("SELECT id,work_entry_id,status FROM submission WHERE id = ?",
                        (rs, rowNum) -> new Submission(rs.getString("id"),
                                rs.getString("work_entry_id"), rs.getString("status")), id)
                .stream().findFirst();
    }

    public Optional<Submission> findByWorkEntryId(String workEntryId) {
        return jdbc.query("SELECT id,work_entry_id,status FROM submission WHERE work_entry_id = ?",
                        (rs, rowNum) -> new Submission(rs.getString("id"),
                                rs.getString("work_entry_id"), rs.getString("status")), workEntryId)
                .stream().findFirst();
    }
}
