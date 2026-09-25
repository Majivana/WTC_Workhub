package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.AuthSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class AuthSessionRepository extends RepositorySupport {
    public AuthSessionRepository(JdbcTemplate jdbc) { super(jdbc); }

    public AuthSession save(AuthSession session) {
        update("INSERT INTO auth_session(id,user_id,token_hash,expires_at,created_at,revoked_at) " +
                        "VALUES (?,?,?,?,?,?)",
                session.id(), session.userId(), session.tokenHash(), session.expiresAt().toString(),
                session.createdAt().toString(), session.revokedAt() == null ? null : session.revokedAt().toString());
        return session;
    }

    public Optional<AuthSession> findActiveByTokenHash(String tokenHash, Instant now) {
        return jdbc.query("SELECT id,user_id,token_hash,expires_at,created_at,revoked_at FROM auth_session " +
                        "WHERE token_hash = ? AND revoked_at IS NULL AND expires_at > ?",
                (rs, rowNum) -> new AuthSession(rs.getString("id"), rs.getString("user_id"),
                        rs.getString("token_hash"), Instant.parse(rs.getString("expires_at")),
                        Instant.parse(rs.getString("created_at")),
                        rs.getString("revoked_at") == null ? null : Instant.parse(rs.getString("revoked_at"))),
                tokenHash, now.toString()).stream().findFirst();
    }

    public void revoke(String tokenHash, Instant revokedAt) {
        update("UPDATE auth_session SET revoked_at = ? WHERE token_hash = ? AND revoked_at IS NULL",
                revokedAt.toString(), tokenHash);
    }
}
