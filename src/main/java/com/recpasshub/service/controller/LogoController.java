package com.recpasshub.service.controller;

import com.recpasshub.service.dto.LogoResponse;
import com.recpasshub.service.service.LogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/logos")
@RequiredArgsConstructor
public class LogoController {

    private final LogoService logoService;

    private void verifyAccess(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is missing");
        }
        String authProviderId = jwt.getSubject();
        if (authProviderId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    /**
     * Uploads a new logo image file.
     *
     * @param file the image file
     * @param jwt  the JWT token
     * @return the created logo metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<LogoResponse> uploadLogo(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(logoService.uploadLogo(file));
    }

    /**
     * Retrieves logo metadata by GUID.
     *
     * @param logoGuid the logo GUID
     * @param jwt        the JWT token
     * @return the logo metadata
     */
    @GetMapping("/{logoGuid}")
    public ResponseEntity<LogoResponse> getLogo(@PathVariable String logoGuid, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.ok(logoService.getLogoMetadata(logoGuid));
    }
}
