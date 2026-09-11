package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.ActivityType;
import co.za.millenniumsolutions.repository.ActivityTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping({"/api/activity-types", "/api/activities"})
public class ActivityTypeController {
    private final ActivityTypeRepository activities;

    public ActivityTypeController(ActivityTypeRepository activities) {
        this.activities = activities;
    }

    @GetMapping
    public List<ActivityType> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return activities.findAll(includeInactive);
    }

    @GetMapping("/{id}")
    public ActivityType get(@PathVariable String id) {
        return activities.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Unknown activity type: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityType create(@RequestBody ActivityTypeRequest request) {
        ActivityType activity = toActivityType(UUID.randomUUID().toString(), request);
        ensureNameAvailable(activity);
        return activities.save(activity);
    }

    @PutMapping("/{id}")
    public ActivityType update(@PathVariable String id, @RequestBody ActivityTypeRequest request) {
        get(id);
        ActivityType activity = toActivityType(id, request);
        ensureNameAvailable(activity);
        return activities.save(activity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        get(id);
        activities.deactivateById(id);
    }

    private ActivityType toActivityType(String id, ActivityTypeRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Activity name is required");
        }
        return new ActivityType(id, request.name().trim(), request.active() == null || request.active());
    }

    private void ensureNameAvailable(ActivityType activity) {
        if (activities.existsByName(activity.name(), activity.id())) {
            throw new IllegalArgumentException("Activity name already exists");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(IllegalArgumentException exception) {
        return new ApiError("INVALID_ACTIVITY_TYPE", exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError missing(NoSuchElementException exception) {
        return new ApiError("NOT_FOUND", exception.getMessage());
    }

    public record ApiError(String code, String message) {}
}
