package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.Escalation;
import co.za.millenniumsolutions.repository.EscalationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/escalations")
public class EscalationController {
    private final EscalationRepository escalations;
    public EscalationController(EscalationRepository escalations) { this.escalations = escalations; }
    @GetMapping
    @PreAuthorize("hasAuthority('PERM_REPORT_READ') || hasRole('SUPERVISOR') || hasRole('MENTOR') || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
    public List<Escalation> open() { return escalations.open(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPERVISOR') || hasRole('MENTOR') || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
    public Escalation create(@RequestBody EscalationRequest request, Authentication authentication) {
        if (request == null || request.reason() == null || request.reason().isBlank())
            throw new IllegalArgumentException("Reason is required");
        return escalations.save(EscalationRepository.create(request.subjectType(), request.subjectId(),
                authentication.getName(), request.severity(), request.reason().trim()));
    }
    public record EscalationRequest(String subjectType,String subjectId,String openedBy,String severity,String reason) {}
}
