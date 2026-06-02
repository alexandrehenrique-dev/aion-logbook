package br.com.byop.aionlogbook.identity.application;

import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.identity.infrastructure.UserProfileRepository;
import br.com.byop.aionlogbook.security.AuthenticatedUser;
import br.com.byop.aionlogbook.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {

    private final AuthenticatedUserProvider authenticatedUserProvider = mock(AuthenticatedUserProvider.class);
    private final UserProfileRepository repository = mock(UserProfileRepository.class);

    private final UserProfileService service = new UserProfileService(
            authenticatedUserProvider,
            repository
    );

    @Nested
    class GetOrCreateCurrentUserProfile {

        @Test
        void shouldCreateUserProfileOnFirstAccess() {
            AuthenticatedUser user = new AuthenticatedUser(
                    "sub-123",
                    "loki@byop.com",
                    "loki",
                    "Loki"
            );

            when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
            when(repository.findByKeycloakSubject("sub-123")).thenReturn(Optional.empty());

            ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);

            when(repository.save(captor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UserProfile result = service.getOrCreateCurrentUserProfile();

            assertThat(result.getId()).isNotNull();
            assertThat(result.getKeycloakSubject()).isEqualTo("sub-123");
            assertThat(result.getEmail()).isEqualTo("loki@byop.com");
            assertThat(result.getUsername()).isEqualTo("loki");
            assertThat(result.getFullName()).isEqualTo("Loki");
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();

            verify(repository).findByKeycloakSubject("sub-123");
            verify(repository).save(any(UserProfile.class));
        }

        @Test
        void shouldReturnExistingUserProfileAndUpdateIdentityData() {
            AuthenticatedUser user = new AuthenticatedUser(
                    "sub-123",
                    "novo@byop.com",
                    "novo-loki",
                    "Novo Loki"
            );

            UserProfile existing = new UserProfile(
                    "sub-123",
                    "antigo@byop.com",
                    "antigo-loki",
                    "Antigo Loki"
            );

            when(authenticatedUserProvider.getCurrentUser()).thenReturn(user);
            when(repository.findByKeycloakSubject("sub-123")).thenReturn(Optional.of(existing));

            UserProfile result = service.getOrCreateCurrentUserProfile();

            assertThat(result.getId()).isEqualTo(existing.getId());
            assertThat(result.getKeycloakSubject()).isEqualTo("sub-123");
            assertThat(result.getEmail()).isEqualTo("novo@byop.com");
            assertThat(result.getUsername()).isEqualTo("novo-loki");
            assertThat(result.getFullName()).isEqualTo("Novo Loki");

            verify(repository).findByKeycloakSubject("sub-123");
            verify(repository, never()).save(any(UserProfile.class));
        }
    }
}