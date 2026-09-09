package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.ProgressService;
import co.za.millenniumsolutions.service.WeeklyProgress;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/users/{userId}/work-periods/{workPeriodId}/progress")
    public WeeklyProgressResponse getProgress(@PathVariable String userId,
                                              @PathVariable String workPeriodId) {
        try {
            return toResponse(progressService.calculate(userId, workPeriodId));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }

    private WeeklyProgressResponse toResponse(WeeklyProgress progress) {
        return new WeeklyProgressResponse(
                progress.targetHours(),
                progress.loggedHours(),
                progress.verifiedHours(),
                progress.pendingHours(),
                progress.remainingHours(),
                progress.percentage(),
                progress.status());
    }
}
