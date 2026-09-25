package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class NotificationRepository extends RepositorySupport {
    public NotificationRepository(JdbcTemplate jdbc) { super(jdbc); }

    public Notification save(Notification n) {
        update("INSERT INTO notification(id,recipient_id,type,title,message,entity_type,entity_id,read_at,created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,COALESCE(?,CURRENT_TIMESTAMP))",
                n.id(), n.recipientId(), n.type(), n.title(), n.message(), n.entityType(), n.entityId(),
                n.readAt() == null ? null : n.readAt().toString(),
                n.createdAt() == null ? null : n.createdAt().toString());
        return n;
    }

    public boolean saveIfAbsent(Notification n) {
        return update("INSERT INTO notification(id,recipient_id,type,title,message,entity_type,entity_id,read_at,created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,COALESCE(?,CURRENT_TIMESTAMP)) ON CONFLICT DO NOTHING",
                n.id(), n.recipientId(), n.type(), n.title(), n.message(), n.entityType(), n.entityId(),
                n.readAt() == null ? null : n.readAt().toString(),
                n.createdAt() == null ? null : n.createdAt().toString()) == 1;
    }

    public List<Notification> findByRecipient(String recipientId) {
        return jdbc.query("SELECT id,recipient_id,type,title,message,entity_type,entity_id,read_at,created_at " +
                        "FROM notification WHERE recipient_id=? ORDER BY created_at DESC",
                (rs, n) -> new Notification(rs.getString("id"), rs.getString("recipient_id"),
                        rs.getString("type"), rs.getString("title"), rs.getString("message"),
                        rs.getString("entity_type"), rs.getString("entity_id"),
                        rs.getString("read_at") == null ? null : Instant.parse(rs.getString("read_at")),
                        Instant.parse(rs.getString("created_at"))), recipientId);
    }

    public void markRead(String id, String recipientId) {
        update("UPDATE notification SET read_at=COALESCE(read_at,CURRENT_TIMESTAMP) " +
                "WHERE id=? AND recipient_id=?", id, recipientId);
    }

    public static Notification create(String recipientId, String type, String title, String message,
                                       String entityType, String entityId) {
        return new Notification(UUID.randomUUID().toString(), recipientId, type, title, message,
                entityType, entityId, null, Instant.now());
    }
}
