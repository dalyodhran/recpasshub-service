package com.recpasshub.service.dto;

import java.io.Serializable;
import java.time.Instant;

// Implements Serializable so it can be safely stored in Redis
public record UserProfileResponse(
    String id, // Exposing the auth provider ID, hiding the internal DB Long ID
    String email,
    String firstName,
    String lastName,
    String type, // Include the user type
    Instant joinedAt
) implements Serializable {}
