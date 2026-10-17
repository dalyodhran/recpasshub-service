package com.recpasshub.service.web.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.recpasshub.service.controller.UserController;
import com.recpasshub.service.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class UserControllerTest {

    private UserService userService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        controller = new UserController(userService);
    }

    private Jwt jwtWith(Map<String, Object> extraClaims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("sub-1")
            .claim("email", "a@b.com")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60));
        extraClaims.forEach(builder::claim);
        return builder.build();
    }

    @Test
    void organizerRoleInRealmAccessAccountRolesRegistersAsOrganizer() {
        Jwt jwt = jwtWith(Map.of("realm_access", Map.of("account", Map.of("roles", List.of("ORGANIZER")))));

        controller.syncUser(jwt);

        verify(userService).syncUser(eq("sub-1"), eq("a@b.com"), any(), any(), eq("ORGANIZER"));
    }

    @Test
    void missingRealmAccessPassesNullUserType() {
        controller.syncUser(jwtWith(Map.of()));

        verify(userService).syncUser(eq("sub-1"), eq("a@b.com"), any(), any(), eq(null));
    }

    @Test
    void rolesWithoutOrganizerPassNullUserType() {
        Jwt jwt = jwtWith(Map.of("realm_access", Map.of("account", Map.of("roles", List.of("manage-account")))));

        controller.syncUser(jwt);

        verify(userService).syncUser(eq("sub-1"), eq("a@b.com"), any(), any(), eq(null));
    }
}
