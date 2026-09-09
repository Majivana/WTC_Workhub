package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.api.WorkEntryRequest;
import co.za.millenniumsolutions.model.WorkEntry;
import co.za.millenniumsolutions.model.WorkPeriod;
import co.za.millenniumsolutions.repository.WorkEntryRepository;
import co.za.millenniumsolutions.repository.WorkPeriodRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class WorkEntryService {

    private final WorkEntryRepository workEntries;
    private final WorkPeriodRepository workPeriods;

    public WorkEntryService(WorkEntryRepository workEntries, WorkPeriodRepository workPeriods) {
        this.workEntries = workEntries;
        this.workPeriods = workPeriods;
    }

    public WorkEntry create(String userId, String workPeriodId, WorkEntryRequest request) {
        WorkPeriod period = period(workPeriodId);
        ValidatedWorkEntry validated = validate(userId, period, request, null);
        return workEntries.save(new WorkEntry(UUID.randomUUID().toString(), userId, workPeriodId,
                request.activityTypeId(), request.workDate(), validated.durationMinutes(),
                "DRAFT", request.startTime(), request.endTime(), validated.breakMinutes()));
    }

    public WorkEntry update(String id, WorkEntryRequest request) {
        WorkEntry existing = workEntries.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Unknown work entry: " + id));
        if (!"DRAFT".equals(existing.status())) {
            throw new IllegalStateException("Only draft work entries can be edited");
        }
        WorkPeriod period = period(existing.workPeriodId());
        ValidatedWorkEntry validated = validate(existing.userId(), period, request, id);
        return workEntries.save(new WorkEntry(existing.id(), existing.userId(), existing.workPeriodId(),
                request.activityTypeId(), request.workDate(), validated.durationMinutes(),
                existing.status(), request.startTime(), request.endTime(), validated.breakMinutes()));
    }

    private WorkPeriod period(String workPeriodId) {
        return workPeriods.findById(workPeriodId)
                .orElseThrow(() -> new NoSuchElementException("Unknown work period: " + workPeriodId));
    }

    private ValidatedWorkEntry validate(String userId, WorkPeriod period, WorkEntryRequest request,
                                        String excludedId) {
        if (userId == null || userId.isBlank() || request == null ||
                request.activityTypeId() == null || request.activityTypeId().isBlank() ||
                request.workDate() == null || request.startTime() == null || request.endTime() == null ||
                request.breakMinutes() == null) {
            throw new IllegalArgumentException("All work-entry fields are required");
        }
        if (request.workDate().isBefore(period.startDate()) ||
                request.workDate().isAfter(period.endDate())) {
            throw new IllegalArgumentException("Work date is outside the work period");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (request.breakMinutes() < 0) {
            throw new IllegalArgumentException("Break minutes cannot be negative");
        }
        int grossMinutes = (int) Duration.between(request.startTime(), request.endTime()).toMinutes();
        if (request.breakMinutes() >= grossMinutes) {
            throw new IllegalArgumentException("Break must be shorter than the work range");
        }
        if (!workEntries.findOverlapping(userId, request.workDate(), request.startTime(),
                request.endTime(), excludedId == null ? "" : excludedId).isEmpty()) {
            throw new IllegalArgumentException("Work entry overlaps an existing entry");
        }
        return new ValidatedWorkEntry(grossMinutes - request.breakMinutes(), request.breakMinutes());
    }

    private record ValidatedWorkEntry(int durationMinutes, int breakMinutes) {
    }
}
