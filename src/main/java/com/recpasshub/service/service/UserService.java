package com.recpasshub.service.service;

import com.recpasshub.service.dto.UserProfileResponse;
import com.recpasshub.service.entity.User;
import com.recpasshub.service.entity.UserType;
import com.recpasshub.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Syncs the user from the JWT.
     * Uses @CachePut to always update the Redis cache with the latest DB state on login.
     */
    @Transactional
    @CachePut(value = "userProfiles", key = "#authProviderId")
    public UserProfileResponse syncUser(String authProviderId, String email, String firstName, String lastName, String typeStr) {
        UserType userType;
        try {
            userType = (typeStr != null && !typeStr.isEmpty()) ? UserType.valueOf(typeStr.toUpperCase()) : UserType.ATTENDEE;
        } catch (IllegalArgumentException e) {
            userType = UserType.ATTENDEE; // fallback if invalid
        }

        final UserType finalUserType = userType;

        User user = userRepository
            .findByAuthProviderId(authProviderId)
            .orElseGet(() ->
                User.builder()
                    .authProviderId(authProviderId)
                    .type(finalUserType) // set initial type
                    .build()
            );

        // Always update mutable fields in case they changed them in Keycloak/Cognito
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        // Also update type in case it changed
        user.setType(finalUserType);

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    /**
     * Standard fetch, cached in Redis for fast access.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userProfiles", key = "#authProviderId")
    public UserProfileResponse getUserProfile(String authProviderId) {
        return userRepository
            .findByAuthProviderId(authProviderId)
            .map(this::mapToResponse)
            .orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "User profile not found. Call sync first."
                )
            );
    }

    private UserProfileResponse mapToResponse(User user) {
        return new UserProfileResponse(
            user.getAuthProviderId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getType().name(),
            user.getCreatedAt()
        );
    }
}
