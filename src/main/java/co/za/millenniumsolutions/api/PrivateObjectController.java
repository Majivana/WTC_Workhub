package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.PrivateObjectAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequestMapping("/api/private-objects")
public class PrivateObjectController {
    private final PrivateObjectAccessService access;

    public PrivateObjectController(PrivateObjectAccessService access) {
        this.access = access;
    }

    @GetMapping("/{objectId}/download")
    @PreAuthorize("isAuthenticated()")
    public DownloadResponse download(@PathVariable String objectId, Authentication authentication) {
        return new DownloadResponse(access.createDownloadUrl(objectId, authentication.getName()));
    }

    @PutMapping("/{objectId}/content")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadContent(@PathVariable String objectId, @RequestBody byte[] content,
                              @RequestHeader(HttpHeaders.CONTENT_TYPE) String mediaType,
                              Authentication authentication) {
        access.storeContent(objectId, authentication.getName(), content, mediaType);
    }

    @GetMapping("/{objectId}/content")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> content(@PathVariable String objectId, Authentication authentication) {
        PrivateObjectAccessService.PrivateContent content = access.readContent(objectId, authentication.getName());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(content.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(content.bytes());
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

        public static final class DownloadResponse {
        private final String url;

        public DownloadResponse(String url) {
            this.url = url;
        }

        public String url() { return url; }
        public String getUrl() { return url; }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof DownloadResponse other)) return false;
            return Objects.equals(url, other.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url);
        }

        @Override
        public String toString() {
            return "DownloadResponse[url=" + url + "]";
        }
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
