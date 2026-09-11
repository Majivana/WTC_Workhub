package co.za.millenniumsolutions.storage;

public record StorageUpload(String objectKey, String uploadUrl, boolean presigned) {
}
