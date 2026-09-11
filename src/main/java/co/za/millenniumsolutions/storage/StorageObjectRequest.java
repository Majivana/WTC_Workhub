package co.za.millenniumsolutions.storage;

import java.util.Objects;

public final class StorageObjectRequest {
    private final StorageObjectType type;
    private final String mediaType;
    private final long sizeBytes;
    private final String checksum;

    public StorageObjectRequest(StorageObjectType type, String mediaType, long sizeBytes, String checksum) {
        this.type = type;
        this.mediaType = mediaType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
    }

    public StorageObjectType type() { return type; }
    public StorageObjectType getType() { return type; }

    public String mediaType() { return mediaType; }
    public String getMediaType() { return mediaType; }

    public long sizeBytes() { return sizeBytes; }
    public long getSizeBytes() { return sizeBytes; }

    public String checksum() { return checksum; }
    public String getChecksum() { return checksum; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof StorageObjectRequest other)) return false;
        return Objects.equals(type, other.type) && Objects.equals(mediaType, other.mediaType) && sizeBytes == other.sizeBytes && Objects.equals(checksum, other.checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, mediaType, sizeBytes, checksum);
    }

    @Override
    public String toString() {
        return "StorageObjectRequest[type=" + type + ", mediaType=" + mediaType + ", sizeBytes=" + sizeBytes + ", checksum=" + checksum + "]";
    }
}
