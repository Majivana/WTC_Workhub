package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.WorkEntry;
import co.za.millenniumsolutions.service.WorkEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class WorkEntryController {

    private final WorkEntryService workEntryService;

    public WorkEntryController(WorkEntryService workEntryService) {
        this.workEntryService = workEntryService;
    }

    @PostMapping("/users/{userId}/work-periods/{workPeriodId}/work-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkEntryResponse create(@PathVariable String userId,
                                    @PathVariable String workPeriodId,
                                    @RequestBody WorkEntryRequest request) {
        return WorkEntryResponse.from(workEntryService.create(userId, workPeriodId, request));
    }

    @PutMapping("/work-entries/{id}")
    public WorkEntryResponse update(@PathVariable String id,
                                    @RequestBody WorkEntryRequest request) {
        return WorkEntryResponse.from(workEntryService.update(id, request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(IllegalArgumentException exception) {
        return new ApiError("INVALID_WORK_ENTRY", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError notEditable(IllegalStateException exception) {
        return new ApiError("WORK_ENTRY_NOT_EDITABLE", exception.getMessage());
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
