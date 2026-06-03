package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.identity.infrastructure.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserProfileRepository userProfileRepository;

    public CurrentUserService(
            AuthenticatedUserProvider authenticatedUserProvider,
            UserProfileRepository userProfileRepository
    ) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public UUID currentUserId() {
        var authenticatedUser = authenticatedUserProvider.getCurrentUser();

        return userProfileRepository
                .findByKeycloakSubject(authenticatedUser.keycloakSubject())
                .orElseThrow(() -> new EntityNotFoundException("User profile not found"))
                .getId();
    }
}