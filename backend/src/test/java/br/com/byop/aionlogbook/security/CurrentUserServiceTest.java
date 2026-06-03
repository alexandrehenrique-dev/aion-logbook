package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.identity.infrastructure.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CurrentUserServiceTest {

    private final AuthenticatedUserProvider authenticatedUserProvider =
            mock(AuthenticatedUserProvider.class);

    private final UserProfileRepository userProfileRepository =
            mock(UserProfileRepository.class);

    private final CurrentUserService service = new CurrentUserService(
            authenticatedUserProvider,
            userProfileRepository
    );

    @Test
    void shouldReturnCurrentUserProfileId() {
        var userId = UUID.randomUUID();
        var keycloakSubject = "keycloak-subject-123";

        var authenticatedUser = new AuthenticatedUser(
                keycloakSubject,
                "user@test.local",
                "user_test",
                "Usuário Teste"
        );

        var userProfile = new UserProfile(
                keycloakSubject,
                "user@test.local",
                "user_test",
                "Usuário Teste"
        );

        setUserProfileId(userProfile, userId);

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(authenticatedUser);
        when(userProfileRepository.findByKeycloakSubject(keycloakSubject))
                .thenReturn(Optional.of(userProfile));

        var result = service.currentUserId();

        assertThat(result).isEqualTo(userId);

        verify(authenticatedUserProvider).getCurrentUser();
        verify(userProfileRepository).findByKeycloakSubject(keycloakSubject);
    }

    @Test
    void shouldThrowWhenUserProfileDoesNotExist() {
        var keycloakSubject = "missing-subject";

        var authenticatedUser = new AuthenticatedUser(
                keycloakSubject,
                "missing@test.local",
                "missing_user",
                "Missing User"
        );

        when(authenticatedUserProvider.getCurrentUser()).thenReturn(authenticatedUser);
        when(userProfileRepository.findByKeycloakSubject(keycloakSubject))
                .thenReturn(Optional.empty());

        assertThatThrownBy(service::currentUserId)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User profile not found");

        verify(authenticatedUserProvider).getCurrentUser();
        verify(userProfileRepository).findByKeycloakSubject(keycloakSubject);
    }

    private static void setUserProfileId(UserProfile userProfile, UUID id) {
        try {
            var field = UserProfile.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(userProfile, id);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalStateException("Could not set UserProfile id for test", exception);
        }
    }
}