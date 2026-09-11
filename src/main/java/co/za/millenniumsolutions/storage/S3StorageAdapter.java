package co.za.millenniumsolutions.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageAdapter implements StoragePort {
    private final S3StorageProperties properties;
    private final S3Presigner presigner;

    public S3StorageAdapter(S3StorageProperties properties) {
        if (properties.bucket().isBlank()) {
            throw new IllegalStateException("app.storage.s3.bucket is required for S3 storage");
        }
        this.properties = properties;
        this.presigner = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .build();
    }

    @Override
    public StorageUpload createUpload(StorageObjectRequest request) {
        StoragePolicy.validate(request);
        String key = request.type().namespace() + "/" + UUID.randomUUID();
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(properties.bucket()).key(key)
                .contentType(request.mediaType().trim().toLowerCase())
                .contentLength(request.sizeBytes())
                .build();
        String url = presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(properties.presignMinutes()))
                        .putObjectRequest(put).build())
                .url().toString();
        return new StorageUpload(key, url, true);
    }

    @Override
    public String createDownloadUrl(String objectKey, Duration lifetime) {
        if (objectKey == null || objectKey.isBlank() || objectKey.contains("..")
                || objectKey.startsWith("/")) {
            throw new IllegalArgumentException("Object key must be private and relative");
        }
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(properties.bucket()).key(objectKey).build();
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(lifetime).getObjectRequest(get).build())
                .url().toString();
    }
}
