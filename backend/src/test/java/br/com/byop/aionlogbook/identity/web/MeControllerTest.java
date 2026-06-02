package br.com.byop.aionlogbook.identity.web;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MeControllerTest {

    private final UserProfileService userProfileService = mock(UserProfileService.class);
    private final MeController controller = new MeController(userProfileService);

    @Nested
    class Me {

        @Test
        void shouldReturnAuthenticatedUserProfile() {
            UserProfile profile = new UserProfile(
                    "sub-123",
                    "loki@byop.com",
                    "loki",
                    "Loki"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            MeResponse response = controller.me();

            assertThat(response.id()).isEqualTo(profile.getId());
            assertThat(response.keycloakSubject()).isEqualTo("sub-123");
            assertThat(response.email()).isEqualTo("loki@byop.com");
            assertThat(response.username()).isEqualTo("loki");
            assertThat(response.fullName()).isEqualTo("Loki");
            assertThat(response.createdAt()).isEqualTo(profile.getCreatedAt());
            assertThat(response.updatedAt()).isEqualTo(profile.getUpdatedAt());

            verify(userProfileService).getOrCreateCurrentUserProfile();
        }
    }
}