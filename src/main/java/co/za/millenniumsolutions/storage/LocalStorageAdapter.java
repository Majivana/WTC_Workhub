package co.za.millenniumsolutions.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageAdapter implements StoragePort {
    @Value("${app.storage.local-directory:./private-object-data}")
    private String localDirectory = "./private-object-data";

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

    @Override
    public void storeObject(String objectKey, byte[] content, String mediaType) {
        try {
            Path path = resolve(objectKey);
            Files.createDirectories(path.getParent());
            Files.write(path, content);
        } catch (IOException exception) {
            throw new IllegalStateException("Private object could not be stored", exception);
        }
    }

    @Override
    public byte[] readObject(String objectKey) {
        try {
            return Files.readAllBytes(resolve(objectKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Private object is unavailable", exception);
        }
    }

    private Path resolve(String objectKey) {
        if (objectKey == null || objectKey.isBlank() || objectKey.startsWith("/") || objectKey.contains("..")) {
            throw new IllegalArgumentException("Object key must be private and relative");
        }
        Path root = Path.of(localDirectory).toAbsolutePath().normalize();
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Object key is outside private storage");
        return target;
    }
}
