package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.Submission;
import co.za.millenniumsolutions.repository.SubmissionRepository;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import co.za.millenniumsolutions.model.AuditLog;
import co.za.millenniumsolutions.repository.AuditLogRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.model.WorkEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class SubmissionService {
    private final SubmissionRepository submissions;
    private final WorkEntryRepository workEntries;
    private final SubmissionNotificationHook notifications;
    private final AuditLogRepository auditLogs;
    private final UserRepository users;

    private static final Map<SubmissionStatus, Set<SubmissionStatus>> TRANSITIONS = Map.of(
            SubmissionStatus.DRAFT, EnumSet.of(SubmissionStatus.SUBMITTED),
            SubmissionStatus.SUBMITTED, EnumSet.of(SubmissionStatus.UNDER_REVIEW),
            SubmissionStatus.UNDER_REVIEW, EnumSet.of(
                    SubmissionStatus.APPROVED, SubmissionStatus.CHANGES_REQUESTED, SubmissionStatus.REJECTED),
            SubmissionStatus.CHANGES_REQUESTED, EnumSet.of(SubmissionStatus.RESUBMITTED),
            SubmissionStatus.RESUBMITTED, EnumSet.of(SubmissionStatus.UNDER_REVIEW)
    );

    public SubmissionService(SubmissionRepository submissions, WorkEntryRepository workEntries,
                             SubmissionNotificationHook notifications, AuditLogRepository auditLogs,
                             UserRepository users) {
        this.submissions = submissions;
        this.workEntries = workEntries;
        this.notifications = notifications;
        this.auditLogs = auditLogs;
        this.users = users;
    }

    @Transactional
    public Submission create(String workEntryId) {
        workEntries.findById(workEntryId)
                .orElseThrow(() -> new NoSuchElementException("Unknown work entry: " + workEntryId));
        return submissions.findByWorkEntryId(workEntryId)
                .orElseGet(() -> submissions.save(new Submission(UUID.randomUUID().toString(),
                        workEntryId, SubmissionStatus.DRAFT.name())));
    }

    public Submission get(String id) {
        return submissions.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Unknown submission: " + id));
    }

    @Transactional
    public Submission transition(String id, String requestedStatus, String actorId) {
        return transition(id, requestedStatus, actorId, true);
    }

    @Transactional
    public Submission transition(String id, String requestedStatus, String actorId, boolean enforceAuthorization) {
        if (requestedStatus == null || requestedStatus.isBlank()) {
            throw new IllegalArgumentException("Target submission status is required");
        }
        Submission previous = get(id);
        SubmissionStatus current = parse(previous.status());
        SubmissionStatus target = parse(requestedStatus);
        User actor = enforceAuthorization
                ? users.findById(actorId)
                .orElseThrow(() -> new SecurityException("Authenticated actor is unavailable"))
                : null;
        WorkEntry worker = workEntries.findById(
                previous.workEntryId()).orElseThrow(() -> new NoSuchElementException("Unknown work entry"));
        boolean finalDecision = target == SubmissionStatus.APPROVED
                || target == SubmissionStatus.REJECTED
                || target == SubmissionStatus.CHANGES_REQUESTED;
        if (enforceAuthorization && finalDecision) {
            boolean reviewer = "SUPERVISOR".equalsIgnoreCase(actor.systemRole())
                    || "MENTOR".equalsIgnoreCase(actor.systemRole())
                    || "ADMIN".equalsIgnoreCase(actor.systemRole())
                    || "SUPER_ADMIN".equalsIgnoreCase(actor.systemRole());
            boolean assigned = actor.id().equals(worker.userId())
                    || actor.id().equals(users.findById(worker.userId()).map(User::supervisorId).orElse(null))
                    || actor.id().equals(users.findById(worker.userId()).map(User::mentorId).orElse(null));
            if (!reviewer || (!"ADMIN".equalsIgnoreCase(actor.systemRole())
                    && !"SUPER_ADMIN".equalsIgnoreCase(actor.systemRole()) && !assigned)) {
                throw new SecurityException("Actor is not assigned to review this submission");
            }
        } else if (enforceAuthorization && !actor.id().equals(worker.userId())
                && !"ADMIN".equalsIgnoreCase(actor.systemRole())
                && !"SUPER_ADMIN".equalsIgnoreCase(actor.systemRole())) {
            throw new SecurityException("Only the submitting user may perform this transition");
        }
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new IllegalStateException("Invalid submission transition from " + current + " to " + target);
        }
        Submission updated = submissions.save(previous.withStatus(target.name()));
        String auditActorId = actorId != null && users.findById(actorId).isPresent() ? actorId : null;
        auditLogs.save(new AuditLog(UUID.randomUUID().toString(), auditActorId, "SUBMISSION", id,
                "STATUS_CHANGED", "from=" + current + ",to=" + target, java.time.Instant.now()));
        notifications.onStatusChanged(previous, updated, actorId);
        return updated;
    }

    private SubmissionStatus parse(String status) {
        try {
            return SubmissionStatus.valueOf(status.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unknown submission status: " + status);
        }
    }
}
