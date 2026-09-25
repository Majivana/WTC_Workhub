package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.EvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.NoSuchElementException;
import java.util.Objects;
@RestController
@RequestMapping("/api/work-entries/{workEntryId}/evidence")
public class EvidenceController {
    private final EvidenceService evidence;

    public EvidenceController(EvidenceService evidence) {
        this.evidence = evidence;
    }

    @PostMapping
    @PreAuthorize("@authorizationService.canAccessWorkEntry(authentication, #workEntryId)")
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceMetadataResponse upload(@PathVariable String workEntryId,
                                           @RequestBody EvidenceUploadRequest request,
                                           Authentication authentication) {
        request.setCreatedBy(authentication.getName());
        return evidence.upload(workEntryId, request);
    }

    @GetMapping
    @PreAuthorize("@authorizationService.canAccessWorkEntry(authentication, #workEntryId)")
    public EvidenceMetadataResponse get(@PathVariable String workEntryId) {
        return evidence.get(workEntryId);
    }

    @PutMapping("/{objectId}/content")
    @PreAuthorize("@authorizationService.canAccessWorkEntry(authentication, #workEntryId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadContent(@PathVariable String workEntryId, @PathVariable String objectId,
                              @RequestBody byte[] content,
                              @RequestHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE) String mediaType,
                              Authentication authentication) {
        evidence.uploadContent(workEntryId, objectId, content, mediaType, authentication.getName());
    }

    @GetMapping("/{objectId}/content")
    @PreAuthorize("@authorizationService.canAccessWorkEntry(authentication, #workEntryId)")
    public org.springframework.http.ResponseEntity<byte[]> downloadContent(
            @PathVariable String workEntryId, @PathVariable String objectId, Authentication authentication) {
        var content = evidence.readContent(workEntryId, objectId, authentication.getName());
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(content.mediaType()))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .header("X-Content-Type-Options", "nosniff")
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(content.bytes());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(IllegalArgumentException exception) {
        return new ApiError("INVALID_EVIDENCE_METADATA", exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError missing(NoSuchElementException exception) {
        return new ApiError("NOT_FOUND", exception.getMessage());
    }

        public static final class ApiError {
        private final String code;
        private final String message;

        public ApiError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String code() { return code; }
        public String getCode() { return code; }

        public String message() { return message; }
        public String getMessage() { return message; }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof ApiError other)) return false;
            return Objects.equals(code, other.code) && Objects.equals(message, other.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, message);
        }

        @Override
        public String toString() {
            return "ApiError[code=" + code + ", message=" + message + "]";
        }
    }
}
