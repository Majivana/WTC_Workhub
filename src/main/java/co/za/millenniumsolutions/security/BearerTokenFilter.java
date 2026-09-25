package co.za.millenniumsolutions.security;

import co.za.millenniumsolutions.model.AuthSession;
import co.za.millenniumsolutions.model.AuthUser;
import co.za.millenniumsolutions.repository.AuthSessionRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.repository.PermissionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {
    private final AuthSessionRepository sessions;
    private final UserRepository users;
    private final PermissionRepository permissions;

    public BearerTokenFilter(AuthSessionRepository sessions, UserRepository users,
                             PermissionRepository permissions) {
        this.sessions = sessions;
        this.users = users;
        this.permissions = permissions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String rawToken = header.substring(7).trim();
            if (!rawToken.isBlank()) {
                sessions.findActiveByTokenHash(hash(rawToken), Instant.now()).flatMap(
                        session -> users.findAuthById(session.userId()))
                        .filter(AuthUser::active)
                        .ifPresent(user -> SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(user.id(), null,
                                        java.util.stream.Stream.concat(
                                                java.util.stream.Stream.of(new SimpleGrantedAuthority(
                                                        "ROLE_" + user.systemRole())),
                                                permissions.findForRole(user.systemRole()).stream()
                                                        .map(code -> new SimpleGrantedAuthority("PERM_" + code)))
                                                .toList())));
            }
        }
        filterChain.doFilter(request, response);
    }

    public static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash authentication token", exception);
        }
    }
}
