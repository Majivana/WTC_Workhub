package co.za.millenniumsolutions.api;

import co.za.millenniumsolutions.model.*;
import co.za.millenniumsolutions.service.ManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("@authorizationService.has(authentication, T(co.za.millenniumsolutions.security.Permission).USER_MANAGE) || hasRole('ADMIN') || hasRole('SUPER_ADMIN')")
public class ManagementController {
    private final ManagementService management;
    public ManagementController(ManagementService management) { this.management = management; }

    @GetMapping("/users")
    public List<User> users(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return management.users(includeInactive);
    }
    @PostMapping("/users") @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody UserManagementRequest request) { return management.createUser(request); }
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable String id, @RequestBody UserManagementRequest request) {
        return management.updateUser(id, request);
    }
    @PostMapping("/users/{id}/deactivate") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable String id) { management.deactivateUser(id); }

    @GetMapping("/institutions")
    @PreAuthorize("@authorizationService.has(authentication, T(co.za.millenniumsolutions.security.Permission).ORGANIZATION_READ)")
    public List<Institution> institutions() { return management.institutions(); }
    @PostMapping("/institutions") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.has(authentication, T(co.za.millenniumsolutions.security.Permission).ORGANIZATION_MANAGE)")
    public Institution createInstitution(@RequestBody InstitutionRequest request) { return management.createInstitution(request); }
    @PutMapping("/institutions/{id}")
    @PreAuthorize("@authorizationService.has(authentication, T(co.za.millenniumsolutions.security.Permission).ORGANIZATION_MANAGE)")
    public Institution updateInstitution(@PathVariable String id, @RequestBody InstitutionRequest request) {
        return management.updateInstitution(id, request);
    }

    @GetMapping("/campuses")
    @PreAuthorize("@authorizationService.has(authentication, T(co.za.millenniumsolutions.security.Permission).ORGANIZATION_READ)")
    public List<Campus> campuses() { return management.campuses(); }
    @PostMapping("/campuses") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@authorizationService.has(authentication, T(co.za.millenniumsolutions.security.Permission).ORGANIZATION_MANAGE)")
    public Campus createCampus(@RequestBody CampusRequest request) { return management.createCampus(request); }
    @PutMapping("/campuses/{id}")
    @PreAuthorize("@authorizationService.has(authentication, T(co.za.millenniumsolutions.security.Permission).ORGANIZATION_MANAGE)")
    public Campus updateCampus(@PathVariable String id, @RequestBody CampusRequest request) {
        return management.updateCampus(id, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ManagementError invalid(IllegalArgumentException e) { return new ManagementError("INVALID_MANAGEMENT_REQUEST", e.getMessage()); }
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ManagementError missing(NoSuchElementException e) { return new ManagementError("NOT_FOUND", e.getMessage()); }
}
