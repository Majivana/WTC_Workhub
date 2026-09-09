package co.za.millenniumsolutions.model;

import java.time.Instant;

public record PrivateObjectReference(String id, String objectKey, String mediaType,
                                     long sizeBytes, String checksum, String purpose,
                                     String createdBy, Instant createdAt) {}
