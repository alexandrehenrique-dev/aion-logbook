package br.com.byop.aionlogbook.identity.api;

import br.com.byop.aionlogbook.identity.domain.UserProfile;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeResponse(
        UUID id,
        String keycloakSubject,
        String email,
        String username,
        String fullName,
        boolean onboardingCompleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static MeResponse from(UserProfile profile) {
        return new MeResponse(
                profile.getId(),
                profile.getKeycloakSubject(),
                profile.getEmail(),
                profile.getUsername(),
                profile.getFullName(),
                profile.isOnboardingCompleted(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
