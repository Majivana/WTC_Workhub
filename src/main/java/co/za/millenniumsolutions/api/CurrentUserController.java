package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class CurrentUserController {
    private final UserRepository users;

    public CurrentUserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public CurrentUserResponse current(Authentication authentication) {
        var user = users.findById(authentication.getName())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Authenticated user is unavailable"));
        List<String> permissions = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("PERM_"))
                .map(authority -> authority.substring("PERM_".length()))
                .sorted()
                .toList();
        return new CurrentUserResponse(user.id(), user.username(), user.displayName(), user.systemRole(),
                user.institutionId(), user.campusId(), user.workRoleId(), user.mentorId(), user.supervisorId(),
                user.active(), permissions);
    }

    public record CurrentUserResponse(String id, String username, String displayName, String systemRole,
                                      String institutionId, String campusId, String workRoleId,
                                      String mentorId, String supervisorId, boolean active,
                                      List<String> permissions) {}
}
