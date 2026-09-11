package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.Evidence;
import co.za.millenniumsolutions.model.EvidenceVersion;
import co.za.millenniumsolutions.model.PrivateObjectReference;

import java.time.Instant;

public record EvidenceMetadataResponse(
        String evidenceId,
        String workEntryId,
        String status,
        int versionNumber,
        String objectKey,
        String mediaType,
        long sizeBytes,
        String checksum,
        String purpose,
        String createdBy,
        Instant uploadedAt,
        String uploadUrl,
        boolean presigned) {
    public static EvidenceMetadataResponse from(Evidence evidence, EvidenceVersion version,
                                                  PrivateObjectReference object,
                                                  String uploadUrl,
                                                  boolean presigned) {
        return new EvidenceMetadataResponse(evidence.id(), evidence.workEntryId(), evidence.status(),
                version.versionNumber(), object.objectKey(), object.mediaType(), object.sizeBytes(),
                version.checksum(), object.purpose(), object.createdBy(), version.uploadedAt(),
                uploadUrl, presigned);
    }
}
