package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.AuditLog;
import co.za.millenniumsolutions.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AttendanceAuditService {
    private final AuditLogRepository audits;
    public AttendanceAuditService(AuditLogRepository audits) { this.audits = audits; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String action, String entity, String details) {
        audits.save(new AuditLog(UUID.randomUUID().toString(), actor, "ATTENDANCE",
                entity, action, details, Instant.now()));
    }
}
