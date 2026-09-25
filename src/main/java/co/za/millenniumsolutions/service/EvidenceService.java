package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.api.EvidenceMetadataResponse;
import co.za.millenniumsolutions.api.EvidenceUploadRequest;
import co.za.millenniumsolutions.model.Evidence;
import co.za.millenniumsolutions.model.EvidenceVersion;
import co.za.millenniumsolutions.model.PrivateObjectReference;
import co.za.millenniumsolutions.repository.EvidenceRepository;
import co.za.millenniumsolutions.repository.EvidenceVersionRepository;
import co.za.millenniumsolutions.repository.AuditLogRepository;
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
    private final AuditLogRepository auditLogs;

    public EvidenceService(WorkEntryRepository workEntries, EvidenceRepository evidence,
                           EvidenceVersionRepository versions, PrivateObjectReferenceRepository objects,
                           StoragePort storage, AuditLogRepository auditLogs) {
        this.workEntries = workEntries;
        this.evidence = evidence;
        this.versions = versions;
        this.objects = objects;
        this.storage = storage;
        this.auditLogs = auditLogs;
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
                versionNumber, objectId, checksum, normalizeNotes(request.changeNotes()), now));
        auditLogs.save(new co.za.millenniumsolutions.model.AuditLog(
                UUID.randomUUID().toString(), request.createdBy(), "EVIDENCE_VERSION", version.id(),
                "UPLOADED", "version=" + versionNumber, now));
        return EvidenceMetadataResponse.from(current, version, object,
                upload.uploadUrl(), upload.presigned());
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
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

    public void uploadContent(String workEntryId, String objectId, byte[] content,
                              String mediaType, String actorId) {
        Evidence current = evidence.findByWorkEntryId(workEntryId)
                .orElseThrow(() -> new NoSuchElementException("No evidence for work entry: " + workEntryId));
        String latestVersionId = evidenceVersionId(current.id());
        EvidenceVersion version = versions.findById(latestVersionId)
                .orElseThrow(() -> new NoSuchElementException("Evidence metadata is incomplete"));
        if (!version.privateObjectReferenceId().equals(objectId)) {
            throw new SecurityException("Object does not belong to the latest evidence version");
        }
        PrivateObjectReference object = objects.findById(objectId)
                .orElseThrow(() -> new NoSuchElementException("Evidence object metadata is incomplete"));
        if (!actorId.equals(object.createdBy())) throw new SecurityException("Only the uploader may complete this upload");
        if (content == null || content.length != object.sizeBytes()) {
            throw new IllegalArgumentException("Uploaded file size does not match its metadata");
        }
        if (!object.mediaType().equalsIgnoreCase(mediaType == null ? "" : mediaType.split(";")[0].trim())) {
            throw new IllegalArgumentException("Uploaded file type does not match its metadata");
        }
        String checksum;
        try {
            checksum = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(content));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
        if (!checksum.equalsIgnoreCase(object.checksum()) || !checksum.equalsIgnoreCase(version.checksum())) {
            throw new IllegalArgumentException("Uploaded file checksum does not match its metadata");
        }
        storage.storeObject(object.objectKey(), content, object.mediaType());
    }

    public PrivateObjectAccessService.PrivateContent readContent(String workEntryId, String objectId, String actorId) {
        Evidence current = evidence.findByWorkEntryId(workEntryId)
                .orElseThrow(() -> new NoSuchElementException("No evidence for work entry: " + workEntryId));
        if (versions.findById(evidenceVersionId(current.id())).map(v -> v.privateObjectReferenceId().equals(objectId)).orElse(false)
                == false) throw new NoSuchElementException("Evidence version not found");
        PrivateObjectReference object = objects.findById(objectId)
                .orElseThrow(() -> new NoSuchElementException("Evidence object metadata is incomplete"));
        return new PrivateObjectAccessService.PrivateContent(object.mediaType(), storage.readObject(object.objectKey()));
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
