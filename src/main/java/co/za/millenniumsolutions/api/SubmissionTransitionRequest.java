package co.za.millenniumsolutions.api;

public class SubmissionTransitionRequest {
    private String status;
    private String actorId;

    public SubmissionTransitionRequest() {
    }

    public SubmissionTransitionRequest(String status, String actorId) {
        this.status = status;
        this.actorId = actorId;
    }

    public String status() { return status; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String actorId() { return actorId; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
}
