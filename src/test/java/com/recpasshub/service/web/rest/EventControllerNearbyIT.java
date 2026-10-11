package com.recpasshub.service.web.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.recpasshub.service.IntegrationTest;
import com.recpasshub.service.entity.Event;
import com.recpasshub.service.entity.EventState;
import com.recpasshub.service.entity.EventType;
import com.recpasshub.service.entity.SportType;
import com.recpasshub.service.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@code GET /api/v1/events/nearby} endpoint.
 */
@AutoConfigureMockMvc
@IntegrationTest
class EventControllerNearbyIT {

    // Dublin, Ireland
    private static final double ORIGIN_LATITUDE = 53.3498;
    private static final double ORIGIN_LONGITUDE = -6.2603;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void cleanup() {
        eventRepository.deleteAll();
    }

    private Event buildEvent(EventState state, Double latitude, Double longitude) {
        return Event.builder()
            .eventName("Test Event")
            .sportType(SportType.RUNNING)
            .eventType(EventType.IN_PERSON)
            .state(state)
            .latitude(latitude)
            .longitude(longitude)
            .build();
    }

    @Test
    @Transactional
    void returnsOnlyPublishedEventsWithinRadius() throws Exception {
        // ~1km from origin, PUBLISHED -> should be returned
        eventRepository.save(buildEvent(EventState.PUBLISHED, 53.3588, ORIGIN_LONGITUDE));
        // Same location, DRAFT -> should be excluded regardless of distance
        eventRepository.save(buildEvent(EventState.DRAFT, ORIGIN_LATITUDE, ORIGIN_LONGITUDE));
        // Far away (London, ~460km), PUBLISHED -> should be excluded by the default 40km radius
        eventRepository.save(buildEvent(EventState.PUBLISHED, 51.5072, -0.1276));

        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", String.valueOf(ORIGIN_LATITUDE))
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
                    .with(jwt())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].state").value("PUBLISHED"));
    }

    @Test
    @Transactional
    void respectsExplicitRadiusBoundary() throws Exception {
        // ~55km from origin, PUBLISHED
        eventRepository.save(buildEvent(EventState.PUBLISHED, 53.8498, ORIGIN_LONGITUDE));

        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", String.valueOf(ORIGIN_LATITUDE))
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
                    .param("radius", "40")
                    .with(jwt())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", String.valueOf(ORIGIN_LATITUDE))
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
                    .param("radius", "60")
                    .with(jwt())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    /**
     * Exercises the east/west side of the bounding box, where a degree of longitude is only
     * ~66km at Dublin's latitude rather than the ~111km a degree of latitude spans.
     */
    @Test
    @Transactional
    void filtersCorrectlyAlongLongitude() throws Exception {
        // ~33km east of origin -> inside the default 40km radius
        eventRepository.save(buildEvent(EventState.PUBLISHED, ORIGIN_LATITUDE, ORIGIN_LONGITUDE + 0.5));
        // ~53km east of origin -> outside 40km, inside 60km
        eventRepository.save(buildEvent(EventState.PUBLISHED, ORIGIN_LATITUDE, ORIGIN_LONGITUDE + 0.8));

        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", String.valueOf(ORIGIN_LATITUDE))
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
                    .with(jwt())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", String.valueOf(ORIGIN_LATITUDE))
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
                    .param("radius", "60")
                    .with(jwt())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Transactional
    void missingLatitudeReturnsBadRequest() throws Exception {
        mockMvc
            .perform(get("/api/v1/events/nearby").param("longitude", String.valueOf(ORIGIN_LONGITUDE)).with(jwt()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void missingLongitudeReturnsBadRequest() throws Exception {
        mockMvc
            .perform(get("/api/v1/events/nearby").param("latitude", String.valueOf(ORIGIN_LATITUDE)).with(jwt()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void nonNumericLatitudeReturnsBadRequest() throws Exception {
        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", "not-a-number")
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
                    .with(jwt())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void latitudeOutOfRangeReturnsBadRequest() throws Exception {
        mockMvc
            .perform(get("/api/v1/events/nearby").param("latitude", "91").param("longitude", String.valueOf(ORIGIN_LONGITUDE)).with(jwt()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void radiusAboveUpperBoundReturnsBadRequest() throws Exception {
        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", String.valueOf(ORIGIN_LATITUDE))
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
                    .param("radius", "201")
                    .with(jwt())
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        mockMvc
            .perform(
                get("/api/v1/events/nearby")
                    .param("latitude", String.valueOf(ORIGIN_LATITUDE))
                    .param("longitude", String.valueOf(ORIGIN_LONGITUDE))
            )
            .andExpect(status().isUnauthorized());
    }
}
