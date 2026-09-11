package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.Evidence;
import co.za.millenniumsolutions.model.EvidenceVersion;
import co.za.millenniumsolutions.model.PrivateObjectReference;

import java.time.Instant;
import java.util.Objects;

public final class EvidenceMetadataResponse {
    private final String evidenceId;
    private final String workEntryId;
    private final String status;
    private final int versionNumber;
    private final String objectKey;
    private final String mediaType;
    private final long sizeBytes;
    private final String checksum;
    private final String purpose;
    private final String createdBy;
    private final Instant uploadedAt;
    private final String uploadUrl;
    private final boolean presigned;

    public EvidenceMetadataResponse(String evidenceId, String workEntryId, String status, int versionNumber,
                                    String objectKey, String mediaType, long sizeBytes, String checksum,
                                    String purpose, String createdBy, Instant uploadedAt, String uploadUrl,
                                    boolean presigned) {
        this.evidenceId = evidenceId;
        this.workEntryId = workEntryId;
        this.status = status;
        this.versionNumber = versionNumber;
        this.objectKey = objectKey;
        this.mediaType = mediaType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.purpose = purpose;
        this.createdBy = createdBy;
        this.uploadedAt = uploadedAt;
        this.uploadUrl = uploadUrl;
        this.presigned = presigned;
    }

    public static EvidenceMetadataResponse from(Evidence evidence, EvidenceVersion version,
                                                 PrivateObjectReference object, String uploadUrl,
                                                 boolean presigned) {
        return new EvidenceMetadataResponse(evidence.id(), evidence.workEntryId(), evidence.status(),
                version.versionNumber(), object.objectKey(), object.mediaType(), object.sizeBytes(),
                version.checksum(), object.purpose(), object.createdBy(), version.uploadedAt(),
                uploadUrl, presigned);
    }

    public String evidenceId() { return evidenceId; }
    public String getEvidenceId() { return evidenceId; }
    public String workEntryId() { return workEntryId; }
    public String getWorkEntryId() { return workEntryId; }
    public String status() { return status; }
    public String getStatus() { return status; }
    public int versionNumber() { return versionNumber; }
    public int getVersionNumber() { return versionNumber; }
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
    public Instant uploadedAt() { return uploadedAt; }
    public Instant getUploadedAt() { return uploadedAt; }
    public String uploadUrl() { return uploadUrl; }
    public String getUploadUrl() { return uploadUrl; }
    public boolean presigned() { return presigned; }
    public boolean isPresigned() { return presigned; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EvidenceMetadataResponse other)) return false;
        return versionNumber == other.versionNumber && sizeBytes == other.sizeBytes && presigned == other.presigned
                && Objects.equals(evidenceId, other.evidenceId) && Objects.equals(workEntryId, other.workEntryId)
                && Objects.equals(status, other.status) && Objects.equals(objectKey, other.objectKey)
                && Objects.equals(mediaType, other.mediaType) && Objects.equals(checksum, other.checksum)
                && Objects.equals(purpose, other.purpose) && Objects.equals(createdBy, other.createdBy)
                && Objects.equals(uploadedAt, other.uploadedAt) && Objects.equals(uploadUrl, other.uploadUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evidenceId, workEntryId, status, versionNumber, objectKey, mediaType, sizeBytes,
                checksum, purpose, createdBy, uploadedAt, uploadUrl, presigned);
    }

    @Override
    public String toString() {
        return "EvidenceMetadataResponse[evidenceId=" + evidenceId + ", workEntryId=" + workEntryId
                + ", status=" + status + ", versionNumber=" + versionNumber + ", objectKey=" + objectKey
                + ", mediaType=" + mediaType + ", sizeBytes=" + sizeBytes + ", checksum=" + checksum
                + ", purpose=" + purpose + ", createdBy=" + createdBy + ", uploadedAt=" + uploadedAt
                + ", uploadUrl=" + uploadUrl + ", presigned=" + presigned + "]";
    }
}
