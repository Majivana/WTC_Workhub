package co.za.millenniumsolutions.model;

import java.time.Instant;

import java.util.Objects;

public final class EvidenceVersion {
    private final String id;
    private final String evidenceId;
    private final int versionNumber;
    private final String privateObjectReferenceId;
    private final String checksum;
    private final Instant uploadedAt;

    public EvidenceVersion(String id, String evidenceId, int versionNumber, String privateObjectReferenceId, String checksum, Instant uploadedAt) {
        this.id = id;
        this.evidenceId = evidenceId;
        this.versionNumber = versionNumber;
        this.privateObjectReferenceId = privateObjectReferenceId;
        this.checksum = checksum;
        this.uploadedAt = uploadedAt;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String evidenceId() { return evidenceId; }
    public String getEvidenceId() { return evidenceId; }

    public int versionNumber() { return versionNumber; }
    public int getVersionNumber() { return versionNumber; }

    public String privateObjectReferenceId() { return privateObjectReferenceId; }
    public String getPrivateObjectReferenceId() { return privateObjectReferenceId; }

    public String checksum() { return checksum; }
    public String getChecksum() { return checksum; }

    public Instant uploadedAt() { return uploadedAt; }
    public Instant getUploadedAt() { return uploadedAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EvidenceVersion other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(evidenceId, other.evidenceId) && versionNumber == other.versionNumber && Objects.equals(privateObjectReferenceId, other.privateObjectReferenceId) && Objects.equals(checksum, other.checksum) && Objects.equals(uploadedAt, other.uploadedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, evidenceId, versionNumber, privateObjectReferenceId, checksum, uploadedAt);
    }

    @Override
    public String toString() {
        return "EvidenceVersion[id=" + id + ", evidenceId=" + evidenceId + ", versionNumber=" + versionNumber + ", privateObjectReferenceId=" + privateObjectReferenceId + ", checksum=" + checksum + ", uploadedAt=" + uploadedAt + "]";
    }
}
