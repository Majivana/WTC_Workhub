package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.model.Submission;

public interface SubmissionNotificationHook {
    void onStatusChanged(Submission previous, Submission current, String actorId);
}
