package co.za.millenniumsolutions.api;

import java.util.Objects;

public class EvidenceUploadRequest {
    private String objectKey;
    private String mediaType;
    private long sizeBytes;
    private String checksum;
    private String purpose;
    private String createdBy;

    public EvidenceUploadRequest() {
    }

    public EvidenceUploadRequest(String objectKey, String mediaType, long sizeBytes, String checksum, String purpose, String createdBy) {
        this.objectKey = objectKey;
        this.mediaType = mediaType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.purpose = purpose;
        this.createdBy = createdBy;
    }

    public String objectKey() { return objectKey; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String mediaType() { return mediaType; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public long sizeBytes() { return sizeBytes; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String checksum() { return checksum; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String purpose() { return purpose; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String createdBy() { return createdBy; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EvidenceUploadRequest other)) return false;
        return Objects.equals(objectKey, other.objectKey) && Objects.equals(mediaType, other.mediaType) && sizeBytes == other.sizeBytes && Objects.equals(checksum, other.checksum) && Objects.equals(purpose, other.purpose) && Objects.equals(createdBy, other.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKey, mediaType, sizeBytes, checksum, purpose, createdBy);
    }

    @Override
    public String toString() {
        return "EvidenceUploadRequest[objectKey=" + objectKey + ", mediaType=" + mediaType + ", sizeBytes=" + sizeBytes + ", checksum=" + checksum + ", purpose=" + purpose + ", createdBy=" + createdBy + "]";
    }
}
