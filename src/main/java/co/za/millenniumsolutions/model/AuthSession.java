package co.za.millenniumsolutions.model;

import java.time.Instant;

public record AuthSession(String id, String userId, String tokenHash, Instant expiresAt,
                          Instant createdAt, Instant revokedAt) {}
