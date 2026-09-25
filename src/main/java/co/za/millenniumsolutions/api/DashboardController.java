package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboards;
    public DashboardController(DashboardService dashboards) { this.dashboards=dashboards; }
    @GetMapping("/users/{userId}/work-periods/{periodId}/dashboard")
    @PreAuthorize("@authorizationService.canAccessUser(authentication, #userId)")
    public DashboardService.StudentDashboard student(@PathVariable String userId,@PathVariable String periodId) {
        return dashboards.student(userId,periodId);
    }
    @GetMapping("/supervisor/queue")
    @PreAuthorize("hasAuthority('PERM_SUBMISSION_REVIEW') || hasRole('SUPERVISOR') || hasRole('MENTOR') || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
    public DashboardService.SupervisorQueue queue(@RequestParam String supervisorId,
                                                  Authentication authentication) {
        boolean strict = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("PERM_"));
        return dashboards.supervisor(strict ? authentication.getName() : supervisorId);
    }
}
