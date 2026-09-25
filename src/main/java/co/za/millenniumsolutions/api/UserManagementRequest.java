package co.za.millenniumsolutions.api;

public record UserManagementRequest(String username, String displayName, String systemRole,
                                    String password, String institutionId, String campusId,
                                    String workRoleId, String mentorId, String supervisorId,
                                    Boolean active) {}
