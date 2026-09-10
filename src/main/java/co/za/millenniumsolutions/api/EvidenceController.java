package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.EvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/work-entries/{workEntryId}/evidence")
public class EvidenceController {
    private final EvidenceService evidence;

    public EvidenceController(EvidenceService evidence) {
        this.evidence = evidence;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenceMetadataResponse upload(@PathVariable String workEntryId,
                                           @RequestBody EvidenceUploadRequest request) {
        return evidence.upload(workEntryId, request);
    }

    @GetMapping
    public EvidenceMetadataResponse get(@PathVariable String workEntryId) {
        return evidence.get(workEntryId);
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

    public record ApiError(String code, String message) {}
}
