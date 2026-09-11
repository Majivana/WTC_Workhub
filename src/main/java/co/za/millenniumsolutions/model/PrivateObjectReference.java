package co.za.millenniumsolutions.model;

import java.time.Instant;

import java.util.Objects;

public final class PrivateObjectReference {
    private final String id;
    private final String objectKey;
    private final String mediaType;
    private final long sizeBytes;
    private final String checksum;
    private final String purpose;
    private final String createdBy;
    private final Instant createdAt;

    public PrivateObjectReference(String id, String objectKey, String mediaType, long sizeBytes, String checksum, String purpose, String createdBy, Instant createdAt) {
        this.id = id;
        this.objectKey = objectKey;
        this.mediaType = mediaType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.purpose = purpose;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String objectKey() { return objectKey; }
    public String getObjectKey() { return objectKey; }

    public String mediaType() { return mediaType; }
    public String getMediaType() { return mediaType; }

    public long sizeBytes() { return sizeBytes; }
    public long getSizeBytes() { return sizeBytes; }

    public String checksum() { return checksum; }
    public String getChecksum() { return checksum; }

    public String purpose() { return purpose; }
    public String getPurpose() { return purpose; }

    public String createdBy() { return createdBy; }
    public String getCreatedBy() { return createdBy; }

    public Instant createdAt() { return createdAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PrivateObjectReference other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(objectKey, other.objectKey) && Objects.equals(mediaType, other.mediaType) && sizeBytes == other.sizeBytes && Objects.equals(checksum, other.checksum) && Objects.equals(purpose, other.purpose) && Objects.equals(createdBy, other.createdBy) && Objects.equals(createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, objectKey, mediaType, sizeBytes, checksum, purpose, createdBy, createdAt);
    }

    @Override
    public String toString() {
        return "PrivateObjectReference[id=" + id + ", objectKey=" + objectKey + ", mediaType=" + mediaType + ", sizeBytes=" + sizeBytes + ", checksum=" + checksum + ", purpose=" + purpose + ", createdBy=" + createdBy + ", createdAt=" + createdAt + "]";
    }
}
