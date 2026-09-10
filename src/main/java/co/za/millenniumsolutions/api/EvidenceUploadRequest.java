package co.za.millenniumsolutions.api;

public record EvidenceUploadRequest(
        String objectKey,
        String mediaType,
        long sizeBytes,
        String checksum,
        String purpose,
        String createdBy) {
}
