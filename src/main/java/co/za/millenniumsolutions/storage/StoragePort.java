package co.za.millenniumsolutions.storage;

import java.time.Duration;

public interface StoragePort {
    StorageUpload createUpload(StorageObjectRequest request);

    String createDownloadUrl(String objectKey, Duration lifetime);

    default void storeObject(String objectKey, byte[] content, String mediaType) {
        throw new UnsupportedOperationException("Storage provider does not support server-side uploads");
    }

    default byte[] readObject(String objectKey) {
        throw new UnsupportedOperationException("Storage provider does not support server-side downloads");
    }
}
