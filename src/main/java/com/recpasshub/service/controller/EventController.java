package com.recpasshub.service.controller;

import com.recpasshub.service.dto.EventRequest;
import com.recpasshub.service.dto.EventResponse;
import com.recpasshub.service.service.EventService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/organizers/events")
@RequiredArgsConstructor
public class EventController {

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
     * Gets a specific event for a user.
     * @param eventId the event ID
     * @param jwt the JWT token
     * @return the event
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable String eventId, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }

    /**
     * Creates a new event.
     * @param request the event request
     * @param jwt the JWT token
     * @return the created event
     */
    @PostMapping
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
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId, @AuthenticationPrincipal Jwt jwt) {
        verifyAccess(jwt);
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
