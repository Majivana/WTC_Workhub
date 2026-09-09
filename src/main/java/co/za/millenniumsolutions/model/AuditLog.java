package co.za.millenniumsolutions.model;

import java.time.Instant;

public record AuditLog(String id, String actorId, String entityType, String entityId,
                       String action, String details, Instant createdAt) {}
