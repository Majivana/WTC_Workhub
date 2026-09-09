package co.za.millenniumsolutions.model;

import java.time.Instant;

public record EvidenceVersion(String id, String evidenceId, int versionNumber,
                              String privateObjectReferenceId, String checksum, Instant uploadedAt) {}
