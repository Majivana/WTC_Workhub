package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import java.time.Instant;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendance;
    public AttendanceController(AttendanceService attendance){this.attendance=attendance;}
    @PostMapping("/clock-in") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.isSelf(authentication, #r.userId())")
    public AttendanceSessionResponse clockIn(@RequestBody AttendanceCaptureRequest r){return AttendanceSessionResponse.from(attendance.clockIn(r));}
    @PostMapping("/{sessionId}/clock-out")
    @PreAuthorize("@authorizationService.canAccessAttendance(authentication, #sessionId)")
    public AttendanceSessionResponse clockOut(@PathVariable String sessionId,@RequestBody AttendanceCaptureRequest r){return AttendanceSessionResponse.from(attendance.clockOut(sessionId,r));}
    @PostMapping("/{sessionId}/corrections")
    @PreAuthorize("hasRole('SUPERVISOR') || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
    public AttendanceSessionResponse correct(@PathVariable String sessionId,@RequestParam String actorId,
            @RequestParam Instant clockInAt,@RequestParam Instant clockOutAt,@RequestParam String reason,
            Authentication authentication){
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().startsWith("PERM_"))) {
            actorId = authentication.getName();
        }
        return AttendanceSessionResponse.from(attendance.correct(sessionId,actorId,clockInAt,clockOutAt,reason));
    }
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalid(IllegalArgumentException e){return new ApiError("INVALID_ATTENDANCE",e.getMessage());}
    @ExceptionHandler(IllegalStateException.class) @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(IllegalStateException e){return new ApiError("ATTENDANCE_CONFLICT",e.getMessage());}
    @ExceptionHandler(SecurityException.class) @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(SecurityException e){return new ApiError("ATTENDANCE_FORBIDDEN",e.getMessage());}
    @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError missing(NoSuchElementException e){return new ApiError("NOT_FOUND",e.getMessage());}
    public record ApiError(String code,String message){public String getCode(){return code;} public String getMessage(){return message;}}
}
