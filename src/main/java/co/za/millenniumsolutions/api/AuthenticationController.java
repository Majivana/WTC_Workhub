package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authentication;
    public AuthenticationController(AuthenticationService authentication) {
        this.authentication = authentication;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authentication.login(request.username(), request.password());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError invalid(IllegalArgumentException exception) {
        return new ApiError("INVALID_CREDENTIALS", "Invalid username or password");
    }

    public record ApiError(String code, String message) {}
}
