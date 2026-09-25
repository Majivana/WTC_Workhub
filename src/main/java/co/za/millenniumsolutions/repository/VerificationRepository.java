package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.Verification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class VerificationRepository extends RepositorySupport {
    public VerificationRepository(JdbcTemplate j) {
        super(j);
    }

    public Verification save(Verification x) {
        update("INSERT INTO verification(id,submission_id,verifier_id,evidence_version_id,action,comment,created_at) " +
                        "VALUES (?,?,?,?,?,?,COALESCE(?,CURRENT_TIMESTAMP))",
                x.id(), x.submissionId(), x.verifierId(), x.evidenceVersionId(), x.action(),
                x.comment(),
                x.createdAt() == null ? null : x.createdAt().toString());
        return x;
    }

    public List<Verification> findBySubmissionId(String submissionId) {
        return jdbc.query("SELECT id,submission_id,verifier_id,evidence_version_id,action,comment,created_at " +
                        "FROM verification WHERE submission_id = ? ORDER BY created_at ASC, id ASC",
                (rs, rowNum) -> new Verification(rs.getString("id"), rs.getString("submission_id"),
                        rs.getString("verifier_id"), rs.getString("evidence_version_id"),
                        rs.getString("action"), rs.getString("comment"),
                        Instant.parse(rs.getString("created_at"))), submissionId);
    }

    public int countBySubmissionId(String submissionId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM verification WHERE submission_id = ?", Integer.class, submissionId);
        return count == null ? 0 : count;
    }
}
