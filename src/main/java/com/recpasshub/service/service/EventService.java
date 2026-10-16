package com.recpasshub.service.service;

import com.recpasshub.service.dto.EventRequest;
import com.recpasshub.service.dto.EventResponse;
import com.recpasshub.service.dto.LogoResponse;
import com.recpasshub.service.dto.MapsResponse;
import com.recpasshub.service.dto.WaiverResponse;
import com.recpasshub.service.entity.Event;
import com.recpasshub.service.entity.EventState;
import com.recpasshub.service.entity.FAQ;
import com.recpasshub.service.repository.EventRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EventService {

    /** Shortest a degree of latitude/longitude ever gets, so the bounding box never clips the circle. */
    private static final double MIN_KM_PER_DEGREE = 110.574;

    private static final double MIN_COS_LATITUDE = 1e-6;

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final EventRepository eventRepository;

    private final TagService tagService;

    /**
     * Gets all events for a specific user.
     *
     * @return list of events
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEventsForUser() {
        return eventRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Gets a single event by ID and user ID.
     *
     * @param eventId the event ID
     * @return the event response
     */
    @Transactional(readOnly = true)
    public EventResponse getEvent(String eventId) {
        return eventRepository
            .findByEventGuid(eventId)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    /**
     * Creates a new event.
     *
     * @param request the event request details
     * @return the created event response
     */
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Event event = Event.builder()
            .organizationGuid(request.organizationGuid())
            .eventName(request.eventName())
            .sportType(request.sportType())
            .eventType(request.eventType())
            .state(request.state() != null ? request.state() : EventState.DRAFT)
            .startDateTime(request.startDateTime())
            .endDateTime(request.endDateTime())
            .capacity(request.capacity())
            .description(request.description())
            .frequentlyAskedQuestions(
                request.frequentlyAskedQuestions() != null ? new ArrayList<>(request.frequentlyAskedQuestions()) : new ArrayList<>()
            )
            .mapGuid(request.mapGuid())
            .waiverGuid(request.waiverGuid())
            .logoGuid(request.logoGuid())
            .build();
        tagService.replaceTags(event, request.tags());

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    /**
     * Updates an existing event.
     *
     * @param eventId the event ID
     * @param request the event request details
     * @return the updated event response
     */
    @Transactional
    public EventResponse updateEvent(String eventId, EventRequest request) {
        Event event = eventRepository
            .findByEventGuid(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        event.setOrganizationGuid(request.organizationGuid());
        event.setEventName(request.eventName());
        event.setSportType(request.sportType());
        event.setEventType(request.eventType());
        event.setState(request.state() != null ? request.state() : EventState.DRAFT);
        event.setStartDateTime(request.startDateTime());
        event.setEndDateTime(request.endDateTime());
        event.setCapacity(request.capacity());
        event.setDescription(request.description());

        if (request.frequentlyAskedQuestions() != null) {
            event.getFrequentlyAskedQuestions().clear();
            event.getFrequentlyAskedQuestions().addAll(request.frequentlyAskedQuestions());
        } else {
            event.getFrequentlyAskedQuestions().clear();
        }

        event.setMapGuid(request.mapGuid());
        event.setWaiverGuid(request.waiverGuid());
        event.setLogoGuid(request.logoGuid());
        tagService.replaceTags(event, request.tags());

        Event updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    /**
     * Deletes an event.
     *
     * @param eventId the event ID
     */
    @Transactional
    public void deleteEvent(String eventId) {
        Event event = eventRepository
            .findByEventGuid(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        eventRepository.delete(event);
    }

    private EventResponse mapToResponse(Event event) {
        // Create a copy of the FAQs to avoid lazy loading issues if accessed outside transaction
        List<FAQ> faqs =
            event.getFrequentlyAskedQuestions() != null ? new ArrayList<>(event.getFrequentlyAskedQuestions()) : new ArrayList<>();

        MapsResponse mapsResponse = null;
        if (event.getMap() != null) {
            mapsResponse = new MapsResponse(
                event.getMap().getMapGuid(),
                event.getMap().getS3FileLocation(),
                event.getMap().getDistance(),
                event.getMap().getElevation(),
                event.getMap().getLocation(),
                event.getMap().getCreatedAt(),
                event.getMap().getUpdatedAt()
            );
        }

        WaiverResponse waiverResponse = null;
        if (event.getWaiver() != null) {
            waiverResponse = new WaiverResponse(
                event.getWaiver().getWaiverGuid(),
                event.getWaiver().getS3FileLocation(),
                event.getWaiver().getCreatedAt(),
                event.getWaiver().getUpdatedAt()
            );
        }

        LogoResponse logoResponse = null;
        if (event.getLogo() != null) {
            logoResponse = new LogoResponse(
                event.getLogo().getLogoGuid(),
                event.getLogo().getS3FileLocation(),
                event.getLogo().getCreatedAt(),
                event.getLogo().getUpdatedAt()
            );
        }

        return new EventResponse(
            event.getEventGuid(),
            event.getOrganizationGuid(),
            event.getEventName(),
            event.getSportType(),
            event.getEventType(),
            event.getState(),
            event.getStartDateTime(),
            event.getEndDateTime(),
            event.getCapacity(),
            event.getDescription(),
            mapsResponse,
            faqs,
            event.getMapGuid(),
            waiverResponse,
            event.getWaiverGuid(),
            logoResponse,
            event.getLogoGuid(),
            tagService.toResponses(event),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }

    /**
     * Finds published events within a radius (in kilometers) of a given coordinate.
     *
     * @param latitude the search origin latitude
     * @param longitude the search origin longitude
     * @param radiusKm the search radius in kilometers
     * @return list of matching event responses
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getNearbyPublishedEvents(double latitude, double longitude, double radiusKm) {
        double latDeltaDegrees = radiusKm / MIN_KM_PER_DEGREE;
        double minLatitude = Math.max(latitude - latDeltaDegrees, -90.0);
        double maxLatitude = Math.min(latitude + latDeltaDegrees, 90.0);

        // A degree of longitude shrinks towards the poles, so the circle's widest longitude
        // span sits at whichever edge of the latitude band is furthest from the equator.
        double widestLatitude = Math.max(Math.abs(minLatitude), Math.abs(maxLatitude));
        double cosLatitude = Math.cos(Math.toRadians(widestLatitude));
        double lonDeltaDegrees = cosLatitude < MIN_COS_LATITUDE ? 180.0 : radiusKm / (MIN_KM_PER_DEGREE * cosLatitude);

        // Near the antimeridian the box would wrap, which a simple BETWEEN cannot express.
        // Fall back to the full longitude range and let the Haversine filter do the work.
        boolean wrapsAntimeridian = longitude - lonDeltaDegrees < -180.0 || longitude + lonDeltaDegrees > 180.0;
        double minLongitude = wrapsAntimeridian ? -180.0 : longitude - lonDeltaDegrees;
        double maxLongitude = wrapsAntimeridian ? 180.0 : longitude + lonDeltaDegrees;

        return eventRepository
            .findByStateAndMap_LatitudeBetweenAndMap_LongitudeBetween(
                EventState.PUBLISHED,
                minLatitude,
                maxLatitude,
                minLongitude,
                maxLongitude
            )
            .stream()
            .filter(e -> haversineKm(latitude, longitude, e.getMap().getLatitude(), e.getMap().getLongitude()) <= radiusKm)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }
}
