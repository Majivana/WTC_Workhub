package co.za.millenniumsolutions.model;

import java.time.Instant;

public record Notification(String id, String recipientId, String type, String title, String message,
                           String entityType, String entityId, Instant readAt, Instant createdAt) {}
