package com.recpasshub.service.controller;

import com.recpasshub.service.dto.WaiverResponse;
import com.recpasshub.service.service.WaiverService;
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
@RequestMapping("/api/v1/waivers")
@RequiredArgsConstructor
public class WaiverController {

    private final WaiverService waiverService;

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
     * Uploads a new waiver PDF file.
     *
     * @param file the PDF file
     * @param jwt  the JWT token
     * @return the created waiver metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<WaiverResponse> uploadWaiver(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(waiverService.uploadWaiver(file));
    }

    /**
     * Retrieves waiver metadata by ID.
     *
     * @param waiverGuid the waiver GUID
     * @param jwt        the JWT token
     * @return the waiver metadata
     */
    @GetMapping("/{waiverGuid}")
    public ResponseEntity<WaiverResponse> getWaiver(@PathVariable String waiverGuid, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.ok(waiverService.getWaiverMetadata(waiverGuid));
    }
}
