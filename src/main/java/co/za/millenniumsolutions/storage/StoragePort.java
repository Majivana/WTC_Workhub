package co.za.millenniumsolutions.storage;

import java.time.Duration;

public interface StoragePort {
    StorageUpload createUpload(StorageObjectRequest request);

    String createDownloadUrl(String objectKey, Duration lifetime);
}
