package br.com.byop.aionlogbook.identity.application;

import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.identity.infrastructure.UserProfileRepository;
import br.com.byop.aionlogbook.security.AuthenticatedUser;
import br.com.byop.aionlogbook.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserProfileRepository repository;

    public UserProfileService(
            AuthenticatedUserProvider authenticatedUserProvider,
            UserProfileRepository repository
    ) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.repository = repository;
    }

    @Transactional
    public UserProfile getOrCreateCurrentUserProfile() {
        AuthenticatedUser user = authenticatedUserProvider.getCurrentUser();

        return repository.findByKeycloakSubject(user.keycloakSubject())
                .map(profile -> {
                    profile.updateFromToken(user.email(), user.username(), user.fullName());
                    return profile;
                })
                .orElseGet(() -> repository.save(new UserProfile(
                        user.keycloakSubject(),
                        user.email(),
                        user.username(),
                        user.fullName()
                )));
    }
}
