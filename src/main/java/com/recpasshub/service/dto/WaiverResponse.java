package com.recpasshub.service.dto;

import java.io.Serializable;
import java.time.Instant;

public record WaiverResponse(String waiverGuid, String s3FileLocation, Instant createdAt, Instant updatedAt) implements Serializable {}
