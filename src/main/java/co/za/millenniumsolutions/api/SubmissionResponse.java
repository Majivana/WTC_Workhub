package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.Submission;

public final class SubmissionResponse {
    private final String id;
    private final String workEntryId;
    private final String status;

    public SubmissionResponse(String id, String workEntryId, String status) {
        this.id = id;
        this.workEntryId = workEntryId;
        this.status = status;
    }

    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(submission.id(), submission.workEntryId(), submission.status());
    }

    public String id() { return id; }
    public String getId() { return id; }
    public String workEntryId() { return workEntryId; }
    public String getWorkEntryId() { return workEntryId; }
    public String status() { return status; }
    public String getStatus() { return status; }
}
