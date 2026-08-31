package com.recpasshub.service.dto;

import java.io.Serializable;
import java.time.Instant;

public record MapsResponse(
    String mapGuid,
    String s3FileLocation,
    Double distance,
    Double elevation,
    String location,
    Instant createdAt,
    Instant updatedAt
) implements Serializable {}
