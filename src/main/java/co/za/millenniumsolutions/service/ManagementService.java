package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.api.*;
import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ManagementService {
    private final UserRepository users;
    private final InstitutionRepository institutions;
    private final CampusRepository campuses;
    private final WorkRoleRepository workRoles;
    private final PasswordEncoder passwords;

    public ManagementService(UserRepository users, InstitutionRepository institutions,
                             CampusRepository campuses, WorkRoleRepository workRoles,
                             PasswordEncoder passwords) {
        this.users = users;
        this.institutions = institutions;
        this.campuses = campuses;
        this.workRoles = workRoles;
        this.passwords = passwords;
    }

    public List<User> users(boolean includeInactive) { return users.findAll(includeInactive); }

    public User createUser(UserManagementRequest request) {
        validateUser(request, null, true);
        String id = UUID.randomUUID().toString();
        User user = toUser(id, request);
        users.save(user);
        if (request.password() != null && !request.password().isBlank()) {
            users.setPasswordHash(id, passwords.encode(request.password()));
        }
        return user;
    }

    public User updateUser(String id, UserManagementRequest request) {
        User existing = users.findById(id).orElseThrow(() -> missing("user", id));
        validateUser(request, id, false);
        User updated = toUser(id, request);
        users.save(updated);
        if (request.password() != null && !request.password().isBlank()) {
            users.setPasswordHash(id, passwords.encode(request.password()));
        }
        return updated;
    }

    public void deactivateUser(String id) {
        User existing = users.findById(id).orElseThrow(() -> missing("user", id));
        if (existing.active()) users.deactivateById(id);
    }

    public List<Institution> institutions() { return institutions.findAll(); }
    public Institution createInstitution(InstitutionRequest request) {
        requireText(request == null ? null : request.name(), "Institution name is required");
        return institutions.save(new Institution(UUID.randomUUID().toString(), request.name().trim()));
    }
    public Institution updateInstitution(String id, InstitutionRequest request) {
        institutions.findById(id).orElseThrow(() -> missing("institution", id));
        requireText(request == null ? null : request.name(), "Institution name is required");
        return institutions.save(new Institution(id, request.name().trim()));
    }

    public List<Campus> campuses() { return campuses.findAll(); }
    public Campus createCampus(CampusRequest request) {
        validateCampus(request);
        return campuses.save(new Campus(UUID.randomUUID().toString(), request.institutionId(), request.name().trim()));
    }
    public Campus updateCampus(String id, CampusRequest request) {
        campuses.findById(id).orElseThrow(() -> missing("campus", id));
        validateCampus(request);
        return campuses.save(new Campus(id, request.institutionId(), request.name().trim()));
    }

    private User toUser(String id, UserManagementRequest r) {
        return new User(id, r.username().trim(), r.displayName().trim(), r.systemRole().trim().toUpperCase(),
                r.workRoleId(), r.institutionId(), r.campusId(), r.mentorId(), r.supervisorId(),
                r.active() == null || r.active());
    }

    private void validateUser(UserManagementRequest r, String id, boolean creating) {
        if (r == null) throw new IllegalArgumentException("User request is required");
        requireText(r.username(), "Username is required");
        requireText(r.displayName(), "Display name is required");
        requireText(r.systemRole(), "System role is required");
        if (creating) requireText(r.password(), "Password is required when creating a user");
        if (!List.of("STUDENT", "SUPERVISOR", "MENTOR", "ADMIN", "SUPER_ADMIN")
                .contains(r.systemRole().trim().toUpperCase())) {
            throw new IllegalArgumentException("Unsupported system role");
        }
        if (r.institutionId() != null) {
            institutions.findById(r.institutionId()).orElseThrow(() -> missing("institution", r.institutionId()));
        }
        if (r.campusId() != null) {
            Campus campus = campuses.findById(r.campusId()).orElseThrow(() -> missing("campus", r.campusId()));
            if (r.institutionId() != null && !r.institutionId().equals(campus.institutionId())) {
                throw new IllegalArgumentException("Campus does not belong to the selected institution");
            }
        }
        if (r.workRoleId() != null) workRoles.findById(r.workRoleId()).orElseThrow(() -> missing("work role", r.workRoleId()));
        validateAssignment(r.mentorId(), id, "mentor");
        validateAssignment(r.supervisorId(), id, "supervisor");
    }

    private void validateAssignment(String assignedId, String subjectId, String label) {
        if (assignedId == null) return;
        if (assignedId.equals(subjectId)) throw new IllegalArgumentException("A user cannot be their own " + label);
        User assigned = users.findById(assignedId).orElseThrow(() -> missing(label, assignedId));
        if (!assigned.active()) throw new IllegalArgumentException("Cannot assign an inactive " + label);
        if ("supervisor".equals(label) && !"SUPERVISOR".equalsIgnoreCase(assigned.systemRole())
                && !"ADMIN".equalsIgnoreCase(assigned.systemRole())) {
            throw new IllegalArgumentException("Assigned supervisor must have supervisor or admin role");
        }
    }

    private void validateCampus(CampusRequest r) {
        if (r == null) throw new IllegalArgumentException("Campus request is required");
        requireText(r.name(), "Campus name is required");
        institutions.findById(r.institutionId()).orElseThrow(() -> missing("institution", r.institutionId()));
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }
    private NoSuchElementException missing(String type, String id) {
        return new NoSuchElementException("Unknown " + type + ": " + id);
    }
}
