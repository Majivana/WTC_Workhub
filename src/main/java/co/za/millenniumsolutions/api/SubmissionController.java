package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class SubmissionController {
    private final SubmissionService submissions;

    public SubmissionController(SubmissionService submissions) {
        this.submissions = submissions;
    }

    @PostMapping("/work-entries/{workEntryId}/submissions")
    @PreAuthorize("@authorizationService.canAccessWorkEntry(authentication, #workEntryId)")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(@PathVariable String workEntryId) {
        return SubmissionResponse.from(submissions.create(workEntryId));
    }

    @GetMapping("/submissions/{id}")
    @PreAuthorize("@authorizationService.canAccessSubmission(authentication, #id)")
    public SubmissionResponse get(@PathVariable String id) {
        return SubmissionResponse.from(submissions.get(id));
    }

    @PostMapping("/submissions/{id}/transitions")
    @PreAuthorize("@authorizationService.canAccessSubmission(authentication, #id)")
    public SubmissionResponse transition(@PathVariable String id,
                                         @RequestBody SubmissionTransitionRequest request,
                                         Authentication authentication) {
        boolean permissionContext = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("PERM_"));
        String actorId = permissionContext ? authentication.getName() : request.actorId();
        return SubmissionResponse.from(submissions.transition(id, request.status(), actorId, permissionContext));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(IllegalArgumentException exception) {
        return new ApiError("INVALID_SUBMISSION", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError invalidTransition(IllegalStateException exception) {
        return new ApiError("INVALID_SUBMISSION_TRANSITION", exception.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(SecurityException exception) {
        return new ApiError("SUBMISSION_FORBIDDEN", exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError missing(NoSuchElementException exception) {
        return new ApiError("NOT_FOUND", exception.getMessage());
    }

    public record ApiError(String code, String message) {
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}
