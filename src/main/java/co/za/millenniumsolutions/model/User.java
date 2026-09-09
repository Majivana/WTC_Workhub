package co.za.millenniumsolutions.model;

public record User(String id, String username, String displayName, String systemRole,
                   String workRoleId, String campusId, boolean active) {}
