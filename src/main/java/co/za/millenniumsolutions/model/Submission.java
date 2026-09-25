package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class Submission {
    private final String id;
    private final String workEntryId;
    private final String status;

    public Submission(String id, String workEntryId, String status) {
        this.id = id;
        this.workEntryId = workEntryId;
        this.status = status;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String workEntryId() { return workEntryId; }
    public String getWorkEntryId() { return workEntryId; }

    public String status() { return status; }
    public String getStatus() { return status; }

    public Submission withStatus(String nextStatus) {
        return new Submission(id, workEntryId, nextStatus);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Submission other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(workEntryId, other.workEntryId) && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workEntryId, status);
    }

    @Override
    public String toString() {
        return "Submission[id=" + id + ", workEntryId=" + workEntryId + ", status=" + status + "]";
    }
}
