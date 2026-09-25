package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.VerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/submissions/{submissionId}/verifications")
public class VerificationController {
    private final VerificationService verifications;

    public VerificationController(VerificationService verifications) {
        this.verifications = verifications;
    }

    @PostMapping
    @PreAuthorize("@authorizationService.canReviewSubmission(authentication, #submissionId)")
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationResponse verify(@PathVariable String submissionId,
                                       @RequestBody VerificationRequest request,
                                       Authentication authentication) {
        boolean strict = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("PERM_"));
        String verifierId = strict ? authentication.getName() : request.verifierId();
        return VerificationResponse.from(verifications.verify(submissionId, verifierId,
                request.evidenceVersionId(), request.action(), request.comment()));
    }

    @GetMapping
    @PreAuthorize("@authorizationService.canAccessSubmission(authentication, #submissionId)")
    public java.util.List<VerificationResponse> history(@PathVariable String submissionId) {
        return verifications.history(submissionId).stream()
                .map(VerificationResponse::from)
                .toList();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(IllegalArgumentException exception) {
        return new ApiError("INVALID_VERIFICATION", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError rejected(IllegalStateException exception) {
        return new ApiError("VERIFICATION_NOT_ALLOWED", exception.getMessage());
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
