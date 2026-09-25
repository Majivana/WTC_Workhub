package co.za.millenniumsolutions.security;

import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import co.za.millenniumsolutions.repository.SubmissionRepository;
import co.za.millenniumsolutions.repository.AttendanceSessionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("authorizationService")
public class AuthorizationService {
    private final UserRepository users;
    private final WorkEntryRepository workEntries;
    private final SubmissionRepository submissions;
    private final AttendanceSessionRepository attendanceSessions;

    public AuthorizationService(UserRepository users, WorkEntryRepository workEntries,
                                SubmissionRepository submissions,
                                AttendanceSessionRepository attendanceSessions) {
        this.users = users;
        this.workEntries = workEntries;
        this.submissions = submissions;
        this.attendanceSessions = attendanceSessions;
    }

    public boolean has(Authentication authentication, Permission permission) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(a -> ("PERM_" + permission.name()).equals(a.getAuthority()));
    }

    public boolean canAccessUser(Authentication authentication, String targetUserId) {
        if (!strict(authentication)) return authentication != null && authentication.isAuthenticated();
        User actor = actor(authentication);
        if (actor == null || targetUserId == null) return false;
        if (actor.id().equals(targetUserId)) return true;
        if ("ADMIN".equalsIgnoreCase(actor.systemRole())
                || "SUPER_ADMIN".equalsIgnoreCase(actor.systemRole())) return true;
        User target = users.findById(targetUserId).orElse(null);
        if (target == null) return false;
        return actor.id().equals(target.supervisorId()) || actor.id().equals(target.mentorId());
    }

    public boolean canReviewUser(Authentication authentication, String targetUserId) {
        if (!strict(authentication)) return authentication != null && authentication.isAuthenticated();
        User actor = actor(authentication);
        return actor != null
                && ("SUPERVISOR".equalsIgnoreCase(actor.systemRole())
                || "MENTOR".equalsIgnoreCase(actor.systemRole())
                || "ADMIN".equalsIgnoreCase(actor.systemRole())
                || "SUPER_ADMIN".equalsIgnoreCase(actor.systemRole()))
                && canAccessUser(authentication, targetUserId);
    }

    public boolean isSelf(Authentication authentication, String userId) {
        if (!strict(authentication)) return authentication != null && authentication.isAuthenticated();
        User actor = actor(authentication);
        return actor != null && actor.id().equals(userId);
    }

    public boolean canAccessWorkEntry(Authentication authentication, String workEntryId) {
        return workEntries.findById(workEntryId)
                .map(entry -> canAccessUser(authentication, entry.userId()))
                .orElse(false);
    }

    public boolean canReviewWorkEntry(Authentication authentication, String workEntryId) {
        return workEntries.findById(workEntryId)
                .map(entry -> canReviewUser(authentication, entry.userId()))
                .orElse(false);
    }

    public boolean canEditWorkEntry(Authentication authentication, String workEntryId) {
        if (!strict(authentication)) {
            return authentication != null && authentication.isAuthenticated();
        }
        return workEntries.findById(workEntryId)
                .map(entry -> canAccessUser(authentication, entry.userId()))
                .orElse(false);
    }

    public boolean canAccessSubmission(Authentication authentication, String submissionId) {
        return submissions.findById(submissionId)
                .map(submission -> canAccessWorkEntry(authentication, submission.workEntryId()))
                .orElse(false);
    }

    public boolean canReviewSubmission(Authentication authentication, String submissionId) {
        return submissions.findById(submissionId)
                .map(submission -> canReviewWorkEntry(authentication, submission.workEntryId()))
                .orElse(false);
    }

    public boolean canAccessAttendance(Authentication authentication, String sessionId) {
        return attendanceSessions.findById(sessionId)
                .map(session -> canAccessUser(authentication, session.userId()))
                .orElse(false);
    }

    public boolean canManageActivity(Authentication authentication) {
        if (!strict(authentication)) return authentication != null && authentication.isAuthenticated();
        return has(authentication, Permission.ORGANIZATION_MANAGE);
    }

    public String actorId(Authentication authentication) {
        User actor = actor(authentication);
        if (actor == null) throw new SecurityException("Authenticated user is unavailable");
        return actor.id();
    }

    private User actor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return users.findById(authentication.getName()).orElse(null);
    }

    private boolean strict(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("PERM_"));
    }
}
