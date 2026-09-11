package co.za.millenniumsolutions.storage;

public record StorageObjectRequest(StorageObjectType type, String mediaType, long sizeBytes,
                                   String checksum) {
}
