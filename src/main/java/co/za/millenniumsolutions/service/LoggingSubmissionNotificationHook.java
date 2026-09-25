package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.Submission;
import co.za.millenniumsolutions.repository.NotificationRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingSubmissionNotificationHook implements SubmissionNotificationHook {
    private static final Logger log = LoggerFactory.getLogger(LoggingSubmissionNotificationHook.class);
    private final NotificationRepository notifications;
    private final UserRepository users;

    public LoggingSubmissionNotificationHook(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Override
    public void onStatusChanged(Submission previous, Submission current, String actorId) {
        log.info("Submission status changed: submissionId={}, workEntryId={}, from={}, to={}, actorId={}",
                current.id(), current.workEntryId(), previous.status(), current.status(), actorId);
        if (actorId != null && !actorId.isBlank() && users.findById(actorId).isPresent()) {
            notifications.save(NotificationRepository.create(actorId, "SUBMISSION_STATUS",
                    "Submission status updated",
                    "Submission " + current.id() + " changed from " + previous.status() + " to " + current.status(),
                    "SUBMISSION", current.id()));
        }
    }
}
