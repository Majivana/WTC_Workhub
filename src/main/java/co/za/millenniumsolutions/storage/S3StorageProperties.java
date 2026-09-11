package co.za.millenniumsolutions.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.s3")
public record S3StorageProperties(String bucket, String region, long presignMinutes) {
    public S3StorageProperties {
        bucket = bucket == null ? "" : bucket;
        region = region == null || region.isBlank() ? "af-south-1" : region;
        presignMinutes = presignMinutes <= 0 ? 10 : presignMinutes;
    }
}
