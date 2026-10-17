package com.recpasshub.service.controller;

import com.recpasshub.service.dto.UserProfileResponse;
import com.recpasshub.service.entity.UserType;
import com.recpasshub.service.service.UserService;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping({ "/registerUser" })
    public ResponseEntity<UserProfileResponse> syncUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Authentication token is missing"
            );
        }

        // Securely extract claims directly from the verified JWT
        String authProviderId = jwt.getSubject();
        if (authProviderId == null || authProviderId.isBlank()) {
            throw new com.recpasshub.service.web.rest.errors.BadRequestAlertException(
                "Invalid JWT: missing subject",
                "userManagement",
                "missingSubject"
            );
        }

        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        String userType = extractUserType(jwt);

        UserProfileResponse response = userService.syncUser(authProviderId, email, firstName, lastName, userType);

        return ResponseEntity.ok(response);
    }

    /**
     * Reads the user type from realm_access.account.roles. Returns null when the role is absent
     * so UserService falls back to ATTENDEE.
     */
    private String extractUserType(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmMap)) {
            return null;
        }
        if (!(realmMap.get("roles") instanceof Collection<?> roles)) {
            return null;
        }
        return roles
            .stream()
            .map(String::valueOf)
            .filter(UserType.ORGANIZER.name()::equalsIgnoreCase)
            .findFirst()
            .map(String::toUpperCase)
            .orElse(null);
    }

    @GetMapping({ "/me" })
    public ResponseEntity<UserProfileResponse> getUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Authentication token is missing"
            );
        }

        String authProviderId = jwt.getSubject();
        if (authProviderId == null || authProviderId.isBlank()) {
            throw new com.recpasshub.service.web.rest.errors.BadRequestAlertException(
                "Invalid JWT: missing subject",
                "userManagement",
                "missingSubject"
            );
        }

        UserProfileResponse response = userService.getUserProfile(authProviderId);
        return ResponseEntity.ok(response);
    }
}
