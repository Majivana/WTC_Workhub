package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.PrivateObjectReference;
import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.repository.PrivateObjectReferenceRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.storage.StoragePort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.NoSuchElementException;

@Service
public class PrivateObjectAccessService {
    private final PrivateObjectReferenceRepository objects;
    private final UserRepository users;
    private final StoragePort storage;

    public PrivateObjectAccessService(PrivateObjectReferenceRepository objects,
                                      UserRepository users, StoragePort storage) {
        this.objects = objects;
        this.users = users;
        this.storage = storage;
    }

    public String createDownloadUrl(String objectId, String actorId) {
        PrivateObjectReference object = objects.findById(objectId)
                .orElseThrow(() -> new NoSuchElementException("Unknown private object: " + objectId));
        User actor = users.findById(actorId)
                .orElseThrow(() -> new NoSuchElementException("Unknown user: " + actorId));
        if (!actor.active() || !(actor.id().equals(object.createdBy())
                || "SUPERVISOR".equals(actor.systemRole())
                || "ADMIN".equals(actor.systemRole())
                || "SUPER_ADMIN".equals(actor.systemRole()))) {
            throw new SecurityException("User is not authorized to access this private object");
        }
        return storage.createDownloadUrl(object.objectKey(), Duration.ofMinutes(10));
    }

    public void storeContent(String objectId, String actorId, byte[] content, String mediaType) {
        PrivateObjectReference object = authorizedObject(objectId, actorId);
        if (content == null || content.length != object.sizeBytes()
                || !object.mediaType().equalsIgnoreCase(mediaType == null ? "" : mediaType.split(";")[0].trim())) {
            throw new IllegalArgumentException("Uploaded content does not match its approved metadata");
        }
        String checksum;
        try {
            checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
        if (!checksum.equalsIgnoreCase(object.checksum())) {
            throw new IllegalArgumentException("Uploaded content checksum does not match its metadata");
        }
        storage.storeObject(object.objectKey(), content, object.mediaType());
    }

    public PrivateContent readContent(String objectId, String actorId) {
        PrivateObjectReference object = authorizedObject(objectId, actorId);
        return new PrivateContent(object.mediaType(), storage.readObject(object.objectKey()));
    }

    private PrivateObjectReference authorizedObject(String objectId, String actorId) {
        PrivateObjectReference object = objects.findById(objectId)
                .orElseThrow(() -> new NoSuchElementException("Unknown private object: " + objectId));
        User actor = users.findById(actorId).orElseThrow(() -> new NoSuchElementException("Unknown user: " + actorId));
        if (!actor.active() || !(actor.id().equals(object.createdBy())
                || "SUPERVISOR".equals(actor.systemRole()) || "ADMIN".equals(actor.systemRole())
                || "SUPER_ADMIN".equals(actor.systemRole()))) {
            throw new SecurityException("User is not authorized to access this private object");
        }
        return object;
    }

    public record PrivateContent(String mediaType, byte[] bytes) {}
}
