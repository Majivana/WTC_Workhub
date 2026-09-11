package co.za.millenniumsolutions.storage;

import java.util.Set;

public final class StoragePolicy {
    public static final long MAX_EVIDENCE_BYTES = 10 * 1024 * 1024;
    public static final long MAX_SELFIE_BYTES = 2 * 1024 * 1024;
    public static final Set<String> EVIDENCE_MEDIA_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/png");
    public static final Set<String> SELFIE_MEDIA_TYPES = Set.of("image/jpeg", "image/png");

    private StoragePolicy() {
    }

    public static void validate(StorageObjectRequest request) {
        if (request == null || request.type() == null || blank(request.mediaType())
                || blank(request.checksum())) {
            throw new IllegalArgumentException("Storage type, media type and checksum are required");
        }
        String mediaType = request.mediaType().trim().toLowerCase();
        Set<String> allowed = request.type() == StorageObjectType.EVIDENCE
                ? EVIDENCE_MEDIA_TYPES : SELFIE_MEDIA_TYPES;
        long maxSize = request.type() == StorageObjectType.EVIDENCE
                ? MAX_EVIDENCE_BYTES : MAX_SELFIE_BYTES;
        if (!allowed.contains(mediaType)) {
            throw new IllegalArgumentException("Unsupported " + request.type().name().toLowerCase()
                    + " media type");
        }
        if (request.sizeBytes() <= 0 || request.sizeBytes() > maxSize) {
            throw new IllegalArgumentException(request.type().name() + " size exceeds policy");
        }
        if (!request.checksum().trim().matches("(?i)^[a-f0-9]{64}$")) {
            throw new IllegalArgumentException("Checksum must be a SHA-256 hexadecimal value");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
