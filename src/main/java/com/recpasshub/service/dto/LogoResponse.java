package com.recpasshub.service.dto;

import java.io.Serializable;
import java.time.Instant;

public record LogoResponse(String logoGuid, String s3FileLocation, Instant createdAt, Instant updatedAt) implements Serializable {}
