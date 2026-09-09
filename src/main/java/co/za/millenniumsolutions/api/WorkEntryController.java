package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.WorkEntry;
import co.za.millenniumsolutions.service.WorkEntryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

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

    public record ApiError(String code, String message) {
    }
}
