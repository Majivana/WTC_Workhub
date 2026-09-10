package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.api.EvidenceMetadataResponse;
import co.za.millenniumsolutions.api.EvidenceUploadRequest;
import co.za.millenniumsolutions.model.Evidence;
import co.za.millenniumsolutions.model.EvidenceVersion;
import co.za.millenniumsolutions.model.PrivateObjectReference;
import co.za.millenniumsolutions.repository.EvidenceRepository;
import co.za.millenniumsolutions.repository.EvidenceVersionRepository;
import co.za.millenniumsolutions.repository.PrivateObjectReferenceRepository;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class EvidenceService {
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_MEDIA_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png");

    private final WorkEntryRepository workEntries;
    private final EvidenceRepository evidence;
    private final EvidenceVersionRepository versions;
    private final PrivateObjectReferenceRepository objects;

    public EvidenceService(WorkEntryRepository workEntries, EvidenceRepository evidence,
                           EvidenceVersionRepository versions, PrivateObjectReferenceRepository objects) {
        this.workEntries = workEntries;
        this.evidence = evidence;
        this.versions = versions;
        this.objects = objects;
    }

    public EvidenceMetadataResponse upload(String workEntryId, EvidenceUploadRequest request) {
        workEntries.findById(workEntryId)
                .orElseThrow(() -> new NoSuchElementException("Unknown work entry: " + workEntryId));
        validate(request);

        Evidence current = evidence.findByWorkEntryId(workEntryId)
                .orElseGet(() -> evidence.save(new Evidence(UUID.randomUUID().toString(), workEntryId, "DRAFT")));
        int versionNumber = versions.nextVersionNumber(current.id());
        String objectId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        PrivateObjectReference object = objects.save(new PrivateObjectReference(objectId, request.objectKey(),
                request.mediaType(), request.sizeBytes(), request.checksum(), request.purpose(),
                request.createdBy(), now));
        EvidenceVersion version = versions.save(new EvidenceVersion(UUID.randomUUID().toString(), current.id(),
                versionNumber, objectId, request.checksum(), now));
        return EvidenceMetadataResponse.from(current, version, object);
    }

    public EvidenceMetadataResponse get(String workEntryId) {
        Evidence current = evidence.findByWorkEntryId(workEntryId)
                .orElseThrow(() -> new NoSuchElementException("No evidence for work entry: " + workEntryId));
        EvidenceVersion version = versions.findById(
                        evidenceVersionId(current.id()))
                .orElseThrow(() -> new NoSuchElementException("Evidence metadata is incomplete"));
        PrivateObjectReference object = objects.findById(version.privateObjectReferenceId())
                .orElseThrow(() -> new NoSuchElementException("Evidence object metadata is incomplete"));
        return EvidenceMetadataResponse.from(current, version, object);
    }

    private String evidenceVersionId(String evidenceId) {
        return versions.findLatestId(evidenceId)
                .orElseThrow(() -> new NoSuchElementException("No evidence version for: " + evidenceId));
    }

    private void validate(EvidenceUploadRequest request) {
        if (request == null || blank(request.objectKey()) || blank(request.mediaType()) ||
                blank(request.checksum()) || blank(request.purpose())) {
            throw new IllegalArgumentException("Object key, media type, checksum and purpose are required");
        }
        if (request.objectKey().startsWith("/") || request.objectKey().contains("..")) {
            throw new IllegalArgumentException("Object key must be a private relative key");
        }
        if (!ALLOWED_MEDIA_TYPES.contains(request.mediaType().toLowerCase())) {
            throw new IllegalArgumentException("Unsupported evidence media type");
        }
        if (request.sizeBytes() <= 0 || request.sizeBytes() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Evidence size must be between 1 byte and 10 MB");
        }
        if (!request.checksum().matches("(?i)^[a-f0-9]{64}$")) {
            throw new IllegalArgumentException("Checksum must be a SHA-256 hexadecimal value");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
