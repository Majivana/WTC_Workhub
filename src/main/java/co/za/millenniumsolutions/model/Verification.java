package co.za.millenniumsolutions.model;

import java.time.Instant;

import java.util.Objects;

public final class Verification {
    private final String id;
    private final String submissionId;
    private final String verifierId;
    private final String evidenceVersionId;
    private final String action;
    private final Instant createdAt;

    public Verification(String id, String submissionId, String verifierId, String evidenceVersionId, String action, Instant createdAt) {
        this.id = id;
        this.submissionId = submissionId;
        this.verifierId = verifierId;
        this.evidenceVersionId = evidenceVersionId;
        this.action = action;
        this.createdAt = createdAt;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String submissionId() { return submissionId; }
    public String getSubmissionId() { return submissionId; }

    public String verifierId() { return verifierId; }
    public String getVerifierId() { return verifierId; }

    public String evidenceVersionId() { return evidenceVersionId; }
    public String getEvidenceVersionId() { return evidenceVersionId; }

    public String action() { return action; }
    public String getAction() { return action; }

    public Instant createdAt() { return createdAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Verification other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(submissionId, other.submissionId) && Objects.equals(verifierId, other.verifierId) && Objects.equals(evidenceVersionId, other.evidenceVersionId) && Objects.equals(action, other.action) && Objects.equals(createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, submissionId, verifierId, evidenceVersionId, action, createdAt);
    }

    @Override
    public String toString() {
        return "Verification[id=" + id + ", submissionId=" + submissionId + ", verifierId=" + verifierId + ", evidenceVersionId=" + evidenceVersionId + ", action=" + action + ", createdAt=" + createdAt + "]";
    }
}
