package co.za.millenniumsolutions.api;

import java.time.Instant;

public record LoginResponse(String token, String userId, String username, String role,
                            Instant expiresAt) {}
