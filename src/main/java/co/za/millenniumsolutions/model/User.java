package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class User {
    private final String id;
    private final String username;
    private final String displayName;
    private final String systemRole;
    private final String workRoleId;
    private final String institutionId;
    private final String campusId;
    private final String mentorId;
    private final String supervisorId;
    private final boolean active;

    public User(String id, String username, String displayName, String systemRole, String workRoleId, String campusId, boolean active) {
        this(id, username, displayName, systemRole, workRoleId, null, campusId, null, null, active);
    }

    public User(String id, String username, String displayName, String systemRole, String workRoleId,
                String institutionId, String campusId, String mentorId, String supervisorId, boolean active) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.systemRole = systemRole;
        this.workRoleId = workRoleId;
        this.institutionId = institutionId;
        this.campusId = campusId;
        this.mentorId = mentorId;
        this.supervisorId = supervisorId;
        this.active = active;
    }

    public String id() { return id; }
    public String getId() { return id; }

    public String username() { return username; }
    public String getUsername() { return username; }

    public String displayName() { return displayName; }
    public String getDisplayName() { return displayName; }

    public String systemRole() { return systemRole; }
    public String getSystemRole() { return systemRole; }

    public String workRoleId() { return workRoleId; }
    public String getWorkRoleId() { return workRoleId; }

    public String institutionId() { return institutionId; }
    public String getInstitutionId() { return institutionId; }

    public String campusId() { return campusId; }
    public String getCampusId() { return campusId; }

    public String mentorId() { return mentorId; }
    public String getMentorId() { return mentorId; }

    public String supervisorId() { return supervisorId; }
    public String getSupervisorId() { return supervisorId; }

    public boolean active() { return active; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof User other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(username, other.username)
                && Objects.equals(displayName, other.displayName) && Objects.equals(systemRole, other.systemRole)
                && Objects.equals(workRoleId, other.workRoleId) && Objects.equals(institutionId, other.institutionId)
                && Objects.equals(campusId, other.campusId) && Objects.equals(mentorId, other.mentorId)
                && Objects.equals(supervisorId, other.supervisorId) && active == other.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, displayName, systemRole, workRoleId, institutionId,
                campusId, mentorId, supervisorId, active);
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + username + ", displayName=" + displayName + ", systemRole=" + systemRole + ", workRoleId=" + workRoleId + ", campusId=" + campusId + ", active=" + active + "]";
    }
}
