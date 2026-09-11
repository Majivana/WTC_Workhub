package co.za.millenniumsolutions.storage;

import java.util.Objects;

public final class StorageUpload {
    private final String objectKey;
    private final String uploadUrl;
    private final boolean presigned;

    public StorageUpload(String objectKey, String uploadUrl, boolean presigned) {
        this.objectKey = objectKey;
        this.uploadUrl = uploadUrl;
        this.presigned = presigned;
    }

    public String objectKey() { return objectKey; }
    public String getObjectKey() { return objectKey; }

    public String uploadUrl() { return uploadUrl; }
    public String getUploadUrl() { return uploadUrl; }

    public boolean presigned() { return presigned; }
    public boolean isPresigned() { return presigned; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof StorageUpload other)) return false;
        return Objects.equals(objectKey, other.objectKey) && Objects.equals(uploadUrl, other.uploadUrl) && presigned == other.presigned;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKey, uploadUrl, presigned);
    }

    @Override
    public String toString() {
        return "StorageUpload[objectKey=" + objectKey + ", uploadUrl=" + uploadUrl + ", presigned=" + presigned + "]";
    }
}
