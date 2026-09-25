package co.za.millenniumsolutions.model;

import java.time.Instant;

public record Escalation(String id, String subjectType, String subjectId, String openedBy,
                         String severity, String status, String reason, String assignedTo,
                         Instant resolvedAt, Instant createdAt) {}
