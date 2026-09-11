package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class Evidence {
    private final String id;
    private final String workEntryId;
    private final String status;

    public Evidence(String id, String workEntryId, String status) {
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Evidence other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(workEntryId, other.workEntryId) && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workEntryId, status);
    }

    @Override
    public String toString() {
        return "Evidence[id=" + id + ", workEntryId=" + workEntryId + ", status=" + status + "]";
    }
}
