package co.za.millenniumsolutions.model;

public record AuthUser(String id, String username, String passwordHash, String systemRole, boolean active) {}
