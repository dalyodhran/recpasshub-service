package com.recpasshub.service.controller;

import com.recpasshub.service.dto.MapsResponse;
import com.recpasshub.service.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
public class MapsController {

    private final MapService mapService;

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
     * Uploads a new map GPX file.
     *
     * @param file the GPX file
     * @param jwt  the JWT token
     * @return the created map metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MapsResponse> uploadMap(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapService.uploadMap(file));
    }

    /**
     * Retrieves map metadata by ID.
     *
     * @param mapGuid the map GUID
     * @param jwt     the JWT token
     * @return the map metadata
     */
    @GetMapping("/{mapGuid}")
    public ResponseEntity<MapsResponse> getMap(@PathVariable String mapGuid, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.ok(mapService.getMapMetadata(mapGuid));
    }
}
