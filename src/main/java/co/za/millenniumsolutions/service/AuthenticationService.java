package co.za.millenniumsolutions.service;

import co.za.millenniumsolutions.api.LoginResponse;
import co.za.millenniumsolutions.model.AuthSession;
import co.za.millenniumsolutions.model.AuthUser;
import co.za.millenniumsolutions.repository.AuthSessionRepository;
import co.za.millenniumsolutions.repository.UserRepository;
import co.za.millenniumsolutions.security.BearerTokenFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserRepository users;
    private final AuthSessionRepository sessions;
    private final PasswordEncoder passwords;

    public AuthenticationService(UserRepository users, AuthSessionRepository sessions,
                                 PasswordEncoder passwords) {
        this.users = users;
        this.sessions = sessions;
        this.passwords = passwords;
    }

    public LoginResponse login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
        AuthUser user = users.findAuthByUsername(username.trim())
                .filter(AuthUser::active)
                .filter(candidate -> candidate.passwordHash() != null
                        && passwords.matches(password, candidate.passwordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        String token = randomToken();
        Instant now = Instant.now();
        Instant expires = now.plus(8, ChronoUnit.HOURS);
        sessions.save(new AuthSession(UUID.randomUUID().toString(), user.id(),
                BearerTokenFilter.hash(token), expires, now, null));
        return new LoginResponse(token, user.id(), user.username(), user.systemRole(), expires);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
