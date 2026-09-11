package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.PrivateObjectReference;
import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.repository.PrivateObjectReferenceRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.storage.StoragePort;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
                || "ADMIN".equals(actor.systemRole()))) {
            throw new SecurityException("User is not authorized to access this private object");
        }
        return storage.createDownloadUrl(object.objectKey(), Duration.ofMinutes(10));
    }
}
