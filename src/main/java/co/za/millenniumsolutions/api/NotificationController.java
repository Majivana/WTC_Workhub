package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.Notification;
import co.za.millenniumsolutions.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/notifications")
public class NotificationController {
    private final NotificationRepository notifications;
    public NotificationController(NotificationRepository notifications) { this.notifications = notifications; }
    @GetMapping
    @PreAuthorize("@authorizationService.isSelf(authentication, #userId) || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
    public List<Notification> list(@PathVariable String userId) { return notifications.findByRecipient(userId); }
    @PostMapping("/{id}/read")
    @PreAuthorize("@authorizationService.isSelf(authentication, #userId) || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
    public void read(@PathVariable String userId, @PathVariable String id) {
        notifications.markRead(id, userId);
    }
}
