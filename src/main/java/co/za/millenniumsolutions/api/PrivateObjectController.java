package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.PrivateObjectAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/private-objects")
public class PrivateObjectController {
    private final PrivateObjectAccessService access;

    public PrivateObjectController(PrivateObjectAccessService access) {
        this.access = access;
    }

    @GetMapping("/{objectId}/download")
    public DownloadResponse download(@PathVariable String objectId,
                                     @RequestParam String actorId) {
        return new DownloadResponse(access.createDownloadUrl(objectId, actorId));
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(SecurityException exception) {
        return new ApiError("FORBIDDEN", exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError missing(NoSuchElementException exception) {
        return new ApiError("NOT_FOUND", exception.getMessage());
    }

    public record DownloadResponse(String url) {
    }

    public record ApiError(String code, String message) {
    }
}
