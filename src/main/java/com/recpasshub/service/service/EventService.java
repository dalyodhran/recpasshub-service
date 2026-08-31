package com.recpasshub.service.service;

import com.recpasshub.service.dto.EventRequest;
import com.recpasshub.service.dto.EventResponse;
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

    private final EventRepository eventRepository;

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
            .build();

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
            faqs,
            event.getMapGuid(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}
