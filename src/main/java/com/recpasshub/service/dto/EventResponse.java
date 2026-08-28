package com.recpasshub.service.dto;

import com.recpasshub.service.entity.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record EventResponse(
    String eventGuid,
    String organizationGuid,
    String eventName,
    SportType sportType,
    EventType eventType,
    EventState state,
    Instant startDateTime,
    Instant endDateTime,
    Integer capacity,
    String description,
    List<FAQ> frequentlyAskedQuestions,
    Instant createdAt,
    Instant updatedAt
) implements Serializable {}
