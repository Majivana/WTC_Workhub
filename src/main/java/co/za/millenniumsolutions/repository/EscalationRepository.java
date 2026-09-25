package co.za.millenniumsolutions.repository;

import co.za.millenniumsolutions.model.Escalation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class EscalationRepository extends RepositorySupport {
    public EscalationRepository(JdbcTemplate jdbc) { super(jdbc); }
    public Escalation save(Escalation e) {
        update("INSERT INTO escalation(id,subject_type,subject_id,opened_by,severity,status,reason,assigned_to,resolved_at,created_at) VALUES (?,?,?,?,?,?,?,?,?,COALESCE(?,CURRENT_TIMESTAMP))",
                e.id(),e.subjectType(),e.subjectId(),e.openedBy(),e.severity(),e.status(),e.reason(),e.assignedTo(),
                e.resolvedAt()==null?null:e.resolvedAt().toString(),e.createdAt()==null?null:e.createdAt().toString());
        return e;
    }
    public List<Escalation> open() {
        return jdbc.query("SELECT id,subject_type,subject_id,opened_by,severity,status,reason,assigned_to,resolved_at,created_at FROM escalation WHERE status <> 'RESOLVED' ORDER BY CASE severity WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END,created_at",
                (rs,n)->new Escalation(rs.getString("id"),rs.getString("subject_type"),rs.getString("subject_id"),rs.getString("opened_by"),rs.getString("severity"),rs.getString("status"),rs.getString("reason"),rs.getString("assigned_to"),rs.getString("resolved_at")==null?null:Instant.parse(rs.getString("resolved_at")),Instant.parse(rs.getString("created_at"))));
    }

    public List<Escalation> findOpenAssignedTo(String userId) {
        return jdbc.query("""
                SELECT id,subject_type,subject_id,opened_by,severity,status,reason,assigned_to,resolved_at,created_at
                FROM escalation WHERE status <> 'RESOLVED' AND assigned_to = ? ORDER BY created_at DESC
                """, (rs, rowNum) -> new Escalation(rs.getString("id"),rs.getString("subject_type"),
                        rs.getString("subject_id"),rs.getString("opened_by"),rs.getString("severity"),
                        rs.getString("status"),rs.getString("reason"),rs.getString("assigned_to"),
                        rs.getString("resolved_at")==null?null:Instant.parse(rs.getString("resolved_at")),
                        Instant.parse(rs.getString("created_at"))), userId);
    }
    public static Escalation create(String subjectType,String subjectId,String openedBy,String severity,String reason) {
        return new Escalation(UUID.randomUUID().toString(),subjectType,subjectId,openedBy,severity,"OPEN",reason,null,null,Instant.now());
    }
}
