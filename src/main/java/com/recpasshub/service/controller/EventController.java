package com.recpasshub.service.controller;

import com.recpasshub.service.dto.EventRequest;
import com.recpasshub.service.dto.EventResponse;
import com.recpasshub.service.service.EventService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private static final String DEFAULT_RADIUS_KM = "40.0";
    private static final double MAX_RADIUS_KM = 200.0;

    private final EventService eventService;

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
     * Gets all events for a user.
     * @param jwt the JWT token
     * @return list of events
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents(@AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.ok(eventService.getAllEventsForUser());
    }

    /**
     * Finds PUBLISHED events within a radius of a given coordinate.
     * @param latitude the search origin latitude, required, -90 to 90
     * @param longitude the search origin longitude, required, -180 to 180
     * @param radius the search radius in kilometers, optional, defaults to {@value #DEFAULT_RADIUS_KM}, max {@value #MAX_RADIUS_KM}
     * @return list of matching events
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<EventResponse>> getNearbyEvents(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam(defaultValue = DEFAULT_RADIUS_KM) double radius
    ) {
        validateLatitude(latitude);
        validateLongitude(longitude);
        validateRadius(radius);
        return ResponseEntity.ok(eventService.getNearbyPublishedEvents(latitude, longitude, radius));
    }

    private void validateLatitude(double latitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "latitude must be between -90 and 90");
        }
    }

    private void validateLongitude(double longitude) {
        if (longitude < -180.0 || longitude > 180.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "longitude must be between -180 and 180");
        }
    }

    private void validateRadius(double radius) {
        if (radius <= 0 || radius > MAX_RADIUS_KM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "radius must be greater than 0 and at most " + MAX_RADIUS_KM + " km");
        }
    }

    /**
     * Gets a specific event for a user.
     * @param eventId the event ID
     * @return the event
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }

    /**
     * Creates a new event.
     * @param request the event request
     * @param jwt the JWT token
     * @return the created event
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    /**
     * Updates an existing event.
     * @param eventId the event ID
     * @param request the event request
     * @param jwt the JWT token
     * @return the updated event
     */
    @PutMapping("/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponse> updateEvent(
        @PathVariable String eventId,
        @Valid @RequestBody EventRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        verifyAccess(jwt);
        return ResponseEntity.ok(eventService.updateEvent(eventId, request));
    }

    /**
     * Deletes an event.
     * @param eventId the event ID
     * @param jwt the JWT token
     * @return no content
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
