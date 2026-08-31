package com.recpasshub.service.dto;

import com.recpasshub.service.entity.EventState;
import com.recpasshub.service.entity.EventType;
import com.recpasshub.service.entity.FAQ;
import com.recpasshub.service.entity.SportType;
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
    String mapGuid,
    Instant createdAt,
    Instant updatedAt
) implements Serializable {}
