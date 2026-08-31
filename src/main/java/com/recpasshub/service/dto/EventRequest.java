package com.recpasshub.service.dto;

import com.recpasshub.service.entity.EventState;
import com.recpasshub.service.entity.EventType;
import com.recpasshub.service.entity.FAQ;
import com.recpasshub.service.entity.SportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record EventRequest(
    String EventId,
    String organizationGuid,

    @NotBlank String eventName,

    @NotNull SportType sportType,

    @NotNull EventType eventType,

    EventState state,

    Instant startDateTime,
    Instant endDateTime,

    Integer capacity,
    String description,
    List<FAQ> frequentlyAskedQuestions,
    String mapGuid
) {}
