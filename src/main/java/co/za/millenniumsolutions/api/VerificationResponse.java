package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.Verification;

import java.time.Instant;

public final class VerificationResponse {
    private final String id;
    private final String submissionId;
    private final String verifierId;
    private final String evidenceVersionId;
    private final String action;
    private final String comment;
    private final Instant createdAt;

    private VerificationResponse(String id, String submissionId, String verifierId,
                                 String evidenceVersionId, String action, String comment, Instant createdAt) {
        this.id = id;
        this.submissionId = submissionId;
        this.verifierId = verifierId;
        this.evidenceVersionId = evidenceVersionId;
        this.action = action;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static VerificationResponse from(Verification x) {
        return new VerificationResponse(x.id(), x.submissionId(), x.verifierId(),
                x.evidenceVersionId(), x.action(), x.comment(), x.createdAt());
    }

    public String getId() { return id; }
    public String getSubmissionId() { return submissionId; }
    public String getVerifierId() { return verifierId; }
    public String getEvidenceVersionId() { return evidenceVersionId; }
    public String getAction() { return action; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
