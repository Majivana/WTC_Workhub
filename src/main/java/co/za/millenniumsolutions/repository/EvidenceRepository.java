package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.Evidence;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EvidenceRepository extends RepositorySupport {
    public EvidenceRepository(JdbcTemplate j) {
        super(j);
    }

    public Evidence save(Evidence x) {
        update("INSERT INTO evidence(id,work_entry_id,status) VALUES (?,?,?) " +
                        "ON CONFLICT(work_entry_id) DO UPDATE SET status=excluded.status",
                x.id(), x.workEntryId(), x.status());
        return findByWorkEntryId(x.workEntryId()).orElse(x);
    }

    public Optional<Evidence> findByWorkEntryId(String workEntryId) {
        return jdbc.query("SELECT id,work_entry_id,status FROM evidence WHERE work_entry_id = ?",
                        (rs, rowNum) -> new Evidence(rs.getString("id"),
                                rs.getString("work_entry_id"), rs.getString("status")), workEntryId)
                .stream().findFirst();
    }
}
