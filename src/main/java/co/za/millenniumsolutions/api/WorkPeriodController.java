package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.WorkPeriod;
import co.za.millenniumsolutions.repository.WorkPeriodRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/work-periods")
public class WorkPeriodController {
    private final WorkPeriodRepository workPeriods;

    public WorkPeriodController(WorkPeriodRepository workPeriods) {
        this.workPeriods = workPeriods;
    }

    @GetMapping
    public List<WorkPeriod> list() {
        return workPeriods.findAll();
    }
}
