package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.PrivateObjectReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class PrivateObjectReferenceRepository extends RepositorySupport {
    public PrivateObjectReferenceRepository(JdbcTemplate j) {
        super(j);
    }

    public PrivateObjectReference save(PrivateObjectReference x) {
        update("INSERT INTO private_object_reference(id,object_key,media_type,size_bytes,checksum,purpose,created_by,created_at) " +
                        "VALUES (?,?,?,?,?,?,?,COALESCE(?,CURRENT_TIMESTAMP)) " +
                        "ON CONFLICT(id) DO UPDATE SET checksum=excluded.checksum",
                x.id(), x.objectKey(), x.mediaType(), x.sizeBytes(), x.checksum(), x.purpose(),
                x.createdBy(), x.createdAt() == null ? null : x.createdAt().toString());
        return findById(x.id()).orElse(x);
    }

    public Optional<PrivateObjectReference> findById(String id) {
        return jdbc.query("SELECT id,object_key,media_type,size_bytes,checksum,purpose,created_by,created_at " +
                        "FROM private_object_reference WHERE id = ?",
                        (rs, rowNum) -> new PrivateObjectReference(rs.getString("id"),
                                rs.getString("object_key"), rs.getString("media_type"),
                                rs.getLong("size_bytes"), rs.getString("checksum"),
                                rs.getString("purpose"), rs.getString("created_by"),
                                Instant.parse(rs.getString("created_at"))), id)
                .stream().findFirst();
    }
}
