package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.AuditLog;
import co.za.millenniumsolutions.model.Submission;
import co.za.millenniumsolutions.model.User;
import co.za.millenniumsolutions.model.Verification;
import co.za.millenniumsolutions.model.WorkEntry;
import co.za.millenniumsolutions.repository.AuditLogRepository;
import co.za.millenniumsolutions.repository.EvidenceVersionRepository;
import co.za.millenniumsolutions.repository.SubmissionRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.repository.VerificationRepository;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.List;

@Service
public class VerificationService {
    private final SubmissionRepository submissions;
    private final VerificationRepository verifications;
    private final EvidenceVersionRepository evidenceVersions;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final SubmissionService submissionService;
    private final WorkEntryRepository workEntries;

    public VerificationService(SubmissionRepository submissions, VerificationRepository verifications,
                                EvidenceVersionRepository evidenceVersions, UserRepository users,
                                AuditLogRepository auditLogs, SubmissionService submissionService,
                                WorkEntryRepository workEntries) {
        this.submissions = submissions;
        this.verifications = verifications;
        this.evidenceVersions = evidenceVersions;
        this.users = users;
        this.auditLogs = auditLogs;
        this.submissionService = submissionService;
        this.workEntries = workEntries;
    }

    @Transactional
    public Verification verify(String submissionId, String verifierId, String evidenceVersionId,
                               String requestedAction, String comment) {
        Submission submission = submissions.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Unknown submission: " + submissionId));
        User verifier = users.findById(verifierId)
                .orElseThrow(() -> new NoSuchElementException("Unknown verifier: " + verifierId));
        if (!verifier.active() || (!"SUPERVISOR".equalsIgnoreCase(verifier.systemRole())
                && !"MENTOR".equalsIgnoreCase(verifier.systemRole())
                && !"ADMIN".equalsIgnoreCase(verifier.systemRole()))) {
            throw new IllegalStateException("Only active assigned mentors, supervisors or administrators can verify submissions");
        }
        String workerId = workEntries.findById(submission.workEntryId())
                .orElseThrow(() -> new NoSuchElementException("Submission work entry is missing"))
                .userId();
        if (workerId.equals(verifierId)) {
            throw new IllegalStateException("A submission cannot be verified by its worker");
        }
        if (!SubmissionStatus.UNDER_REVIEW.name().equals(submission.status())) {
            throw new IllegalStateException("Only submissions under review can be verified");
        }
        if (evidenceVersionId == null || evidenceVersionId.isBlank()
                || evidenceVersions.findById(evidenceVersionId).isEmpty()
                || !evidenceVersions.belongsToWorkEntry(evidenceVersionId, submission.workEntryId())) {
            throw new IllegalArgumentException("A valid evidence version for the submission is required");
        }
        VerificationAction action = parseAction(requestedAction);
        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("Verification comment is required");
        }
        String targetStatus = switch (action) {
            case APPROVE -> SubmissionStatus.APPROVED.name();
            case REQUEST_CHANGES -> SubmissionStatus.CHANGES_REQUESTED.name();
            case REJECT -> SubmissionStatus.REJECTED.name();
        };
        Instant now = Instant.now();
        Verification verification = new Verification(UUID.randomUUID().toString(), submissionId, verifierId,
                evidenceVersionId, action.name(), comment.trim(), now);
        Verification saved = verifications.save(verification);
        submissionService.transition(submissionId, targetStatus, verifierId, false);
        if (action == VerificationAction.APPROVE) {
            WorkEntry entry = workEntries.findById(submission.workEntryId())
                    .orElseThrow(() -> new NoSuchElementException("Submission work entry is missing"));
            workEntries.save(new WorkEntry(entry.id(), entry.userId(), entry.workPeriodId(),
                    entry.activityTypeId(), entry.workDate(), entry.durationMinutes(), "APPROVED",
                    entry.startTime(), entry.endTime(), entry.breakMinutes()));
        }
        auditLogs.save(new AuditLog(UUID.randomUUID().toString(), verifierId, "VERIFICATION",
                saved.id(), action.name(), "submissionId=" + submissionId
                + ",evidenceVersionId=" + evidenceVersionId, now));
        return saved;
    }

    public List<Verification> history(String submissionId) {
        if (submissions.findById(submissionId).isEmpty()) {
            throw new NoSuchElementException("Unknown submission: " + submissionId);
        }
        return verifications.findBySubmissionId(submissionId);
    }

    private VerificationAction parseAction(String action) {
        try {
            return VerificationAction.valueOf(action.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unknown verification action: " + action);
        }
    }
}
