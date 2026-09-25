package co.za.millenniumsolutions.api;

public class VerificationRequest {
    private String verifierId;
    private String evidenceVersionId;
    private String action;
    private String comment;

    public VerificationRequest() {
    }

    public String verifierId() { return verifierId; }
    public String getVerifierId() { return verifierId; }
    public void setVerifierId(String verifierId) { this.verifierId = verifierId; }

    public String evidenceVersionId() { return evidenceVersionId; }
    public String getEvidenceVersionId() { return evidenceVersionId; }
    public void setEvidenceVersionId(String evidenceVersionId) { this.evidenceVersionId = evidenceVersionId; }

    public String action() { return action; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String comment() { return comment; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
