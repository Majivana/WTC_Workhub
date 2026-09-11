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
import co.za.millenniumsolutions.storage.StorageObjectRequest;
import co.za.millenniumsolutions.storage.StorageObjectType;
import co.za.millenniumsolutions.storage.StoragePolicy;
import co.za.millenniumsolutions.storage.StoragePort;
import co.za.millenniumsolutions.storage.StorageUpload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class EvidenceService {
    private final WorkEntryRepository workEntries;
    private final EvidenceRepository evidence;
    private final EvidenceVersionRepository versions;
    private final PrivateObjectReferenceRepository objects;
    private final StoragePort storage;

    public EvidenceService(WorkEntryRepository workEntries, EvidenceRepository evidence,
                           EvidenceVersionRepository versions, PrivateObjectReferenceRepository objects,
                           StoragePort storage) {
        this.workEntries = workEntries;
        this.evidence = evidence;
        this.versions = versions;
        this.objects = objects;
        this.storage = storage;
    }

    @Transactional
    public EvidenceMetadataResponse upload(String workEntryId, EvidenceUploadRequest request) {
        workEntries.findById(workEntryId)
                .orElseThrow(() -> new NoSuchElementException("Unknown work entry: " + workEntryId));
        validate(request);

        String mediaType = request.mediaType().trim().toLowerCase();
        String purpose = request.purpose().trim();
        String checksum = request.checksum().trim().toLowerCase();
        StorageUpload upload = storage.createUpload(new StorageObjectRequest(
                StorageObjectType.EVIDENCE, mediaType, request.sizeBytes(), checksum));
        Evidence current = evidence.findByWorkEntryId(workEntryId)
                .orElseGet(() -> evidence.save(new Evidence(UUID.randomUUID().toString(), workEntryId, "DRAFT")));
        int versionNumber = versions.nextVersionNumber(current.id());
        String objectId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        PrivateObjectReference object = objects.save(new PrivateObjectReference(objectId, upload.objectKey(),
                mediaType, request.sizeBytes(), checksum, purpose,
                request.createdBy(), now));
        EvidenceVersion version = versions.save(new EvidenceVersion(UUID.randomUUID().toString(), current.id(),
                versionNumber, objectId, checksum, now));
        return EvidenceMetadataResponse.from(current, version, object,
                upload.uploadUrl(), upload.presigned());
    }

    public EvidenceMetadataResponse get(String workEntryId) {
        Evidence current = evidence.findByWorkEntryId(workEntryId)
                .orElseThrow(() -> new NoSuchElementException("No evidence for work entry: " + workEntryId));
        EvidenceVersion version = versions.findById(
                        evidenceVersionId(current.id()))
                .orElseThrow(() -> new NoSuchElementException("Evidence metadata is incomplete"));
        PrivateObjectReference object = objects.findById(version.privateObjectReferenceId())
                .orElseThrow(() -> new NoSuchElementException("Evidence object metadata is incomplete"));
        return EvidenceMetadataResponse.from(current, version, object, null, false);
    }

    private String evidenceVersionId(String evidenceId) {
        return versions.findLatestId(evidenceId)
                .orElseThrow(() -> new NoSuchElementException("No evidence version for: " + evidenceId));
    }

    private void validate(EvidenceUploadRequest request) {
        if (request == null || blank(request.mediaType()) ||
                blank(request.checksum()) || blank(request.purpose())) {
            throw new IllegalArgumentException("Media type, checksum and purpose are required");
        }
        StoragePolicy.validate(new StorageObjectRequest(StorageObjectType.EVIDENCE,
                request.mediaType(), request.sizeBytes(), request.checksum()));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
