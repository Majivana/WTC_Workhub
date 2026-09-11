package co.za.millenniumsolutions.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

@ConfigurationProperties(prefix = "app.storage.s3")
public final class S3StorageProperties {
    private final String bucket;
    private final String region;
    private final long presignMinutes;

    public S3StorageProperties(String bucket, String region, long presignMinutes) {
        this.bucket = bucket == null ? "" : bucket;
        this.region = region == null || region.isBlank() ? "af-south-1" : region;
        this.presignMinutes = presignMinutes <= 0 ? 10 : presignMinutes;
    }

    public String bucket() { return bucket; }
    public String getBucket() { return bucket; }
    public String region() { return region; }
    public String getRegion() { return region; }
    public long presignMinutes() { return presignMinutes; }
    public long getPresignMinutes() { return presignMinutes; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof S3StorageProperties other)) return false;
        return presignMinutes == other.presignMinutes
                && Objects.equals(bucket, other.bucket) && Objects.equals(region, other.region);
    }

    @Override
    public int hashCode() { return Objects.hash(bucket, region, presignMinutes); }

    @Override
    public String toString() {
        return "S3StorageProperties[bucket=" + bucket + ", region=" + region
                + ", presignMinutes=" + presignMinutes + "]";
    }
}
