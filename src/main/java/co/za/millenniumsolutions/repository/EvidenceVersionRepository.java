package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.EvidenceVersion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class EvidenceVersionRepository extends RepositorySupport {
    public EvidenceVersionRepository(JdbcTemplate j) {
        super(j);
    }

    public EvidenceVersion save(EvidenceVersion x) {
        update("INSERT INTO evidence_version(id,evidence_id,version_number,private_object_reference_id,checksum,uploaded_at) " +
                        "VALUES (?,?,?,?,?,COALESCE(?,CURRENT_TIMESTAMP))",
                x.id(), x.evidenceId(), x.versionNumber(), x.privateObjectReferenceId(), x.checksum(),
                x.uploadedAt() == null ? null : x.uploadedAt().toString());
        return findById(x.id()).orElse(x);
    }

    public Optional<EvidenceVersion> findById(String id) {
        return jdbc.query("SELECT id,evidence_id,version_number,private_object_reference_id,checksum,uploaded_at " +
                        "FROM evidence_version WHERE id = ?",
                        (rs, rowNum) -> new EvidenceVersion(rs.getString("id"), rs.getString("evidence_id"),
                                rs.getInt("version_number"), rs.getString("private_object_reference_id"),
                                rs.getString("checksum"), Instant.parse(rs.getString("uploaded_at"))), id)
                .stream().findFirst();
    }

    public Optional<String> findLatestId(String evidenceId) {
        return jdbc.query("SELECT id FROM evidence_version WHERE evidence_id = ? " +
                        "ORDER BY version_number DESC LIMIT 1",
                        (rs, rowNum) -> rs.getString("id"), evidenceId)
                .stream().findFirst();
    }

    public int nextVersionNumber(String evidenceId) {
        Integer version = jdbc.queryForObject(
                "SELECT COALESCE(MAX(version_number), 0) + 1 FROM evidence_version WHERE evidence_id = ?",
                Integer.class, evidenceId);
        return version == null ? 1 : version;
    }
}
