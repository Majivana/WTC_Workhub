package co.za.millenniumsolutions.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@Profile("local")
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements StoragePort {
    @Override
    public StorageUpload createUpload(StorageObjectRequest request) {
        StoragePolicy.validate(request);
        String key = request.type().namespace() + "/" + UUID.randomUUID();
        return new StorageUpload(key, "local-storage://" + key, false);
    }

    @Override
    public String createDownloadUrl(String objectKey, Duration lifetime) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("Object key is required");
        }
        return "local-storage://" + objectKey;
    }
}
