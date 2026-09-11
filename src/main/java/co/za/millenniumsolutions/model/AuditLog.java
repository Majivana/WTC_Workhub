package co.za.millenniumsolutions.model;

import java.time.Instant;

import java.util.Objects;

public final class AuditLog {
    private final String id;
    private final String actorId;
    private final String entityType;
    private final String entityId;
    private final String action;
    private final String details;
    private final Instant createdAt;

    public AuditLog(String id, String actorId, String entityType, String entityId, String action, String details, Instant createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String actorId() { return actorId; }
    public String getActorId() { return actorId; }

    public String entityType() { return entityType; }
    public String getEntityType() { return entityType; }

    public String entityId() { return entityId; }
    public String getEntityId() { return entityId; }

    public String action() { return action; }
    public String getAction() { return action; }

    public String details() { return details; }
    public String getDetails() { return details; }

    public Instant createdAt() { return createdAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AuditLog other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(actorId, other.actorId) && Objects.equals(entityType, other.entityType) && Objects.equals(entityId, other.entityId) && Objects.equals(action, other.action) && Objects.equals(details, other.details) && Objects.equals(createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, actorId, entityType, entityId, action, details, createdAt);
    }

    @Override
    public String toString() {
        return "AuditLog[id=" + id + ", actorId=" + actorId + ", entityType=" + entityType + ", entityId=" + entityId + ", action=" + action + ", details=" + details + ", createdAt=" + createdAt + "]";
    }
}
