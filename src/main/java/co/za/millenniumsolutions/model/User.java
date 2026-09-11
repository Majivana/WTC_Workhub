package co.za.millenniumsolutions.model;

import java.util.Objects;

public final class User {
    private final String id;
    private final String username;
    private final String displayName;
    private final String systemRole;
    private final String workRoleId;
    private final String campusId;
    private final boolean active;

    public User(String id, String username, String displayName, String systemRole, String workRoleId, String campusId, boolean active) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.systemRole = systemRole;
        this.workRoleId = workRoleId;
        this.campusId = campusId;
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

    public String campusId() { return campusId; }
    public String getCampusId() { return campusId; }

    public boolean active() { return active; }
    public boolean isActive() { return active; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof User other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(username, other.username) && Objects.equals(displayName, other.displayName) && Objects.equals(systemRole, other.systemRole) && Objects.equals(workRoleId, other.workRoleId) && Objects.equals(campusId, other.campusId) && active == other.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, displayName, systemRole, workRoleId, campusId, active);
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + username + ", displayName=" + displayName + ", systemRole=" + systemRole + ", workRoleId=" + workRoleId + ", campusId=" + campusId + ", active=" + active + "]";
    }
}
