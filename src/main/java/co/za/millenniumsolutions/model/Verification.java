package co.za.millenniumsolutions.model;

import java.time.Instant;

public record Verification(String id, String submissionId, String verifierId,
                           String evidenceVersionId, String action, Instant createdAt) {}
