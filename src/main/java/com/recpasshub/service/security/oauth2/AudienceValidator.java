package com.recpasshub.service.security.oauth2;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final Logger LOG = LoggerFactory.getLogger(AudienceValidator.class);
    private final OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);

    private final List<String> allowedAudience;

    public AudienceValidator(List<String> allowedAudience) {
        Assert.notEmpty(allowedAudience, "Allowed audience should not be null or empty.");
        this.allowedAudience = allowedAudience;
    }

    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        if (audience != null && audience.stream().anyMatch(allowedAudience::contains)) {
            return OAuth2TokenValidatorResult.success();
        }

        String azp = jwt.getClaimAsString("azp");
        if (azp != null && allowedAudience.contains(azp)) {
            return OAuth2TokenValidatorResult.success();
        }

        String clientId = jwt.getClaimAsString("client_id");
        if (clientId != null && allowedAudience.contains(clientId)) {
            return OAuth2TokenValidatorResult.success();
        }

        LOG.warn("Invalid audience: {}, azp: {}, or client_id: {}", audience, azp, clientId);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
