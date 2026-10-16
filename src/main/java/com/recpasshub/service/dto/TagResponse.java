package com.recpasshub.service.dto;

import com.recpasshub.service.entity.SportType;
import java.io.Serializable;
import java.time.Instant;

public record TagResponse(String tagGuid, String name, SportType sportType, Instant createdAt) implements Serializable {}
