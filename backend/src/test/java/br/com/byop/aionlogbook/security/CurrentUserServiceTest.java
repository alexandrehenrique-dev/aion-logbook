package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CurrentUserServiceTest {

    private final UserProfileService userProfileService = mock(UserProfileService.class);
    private final CurrentUserService service = new CurrentUserService(userProfileService);

    @Nested
    class CurrentUserId {

        @Test
        void shouldReturnIdFromExistingProfile() {
            var profile = new UserProfile("sub-123", "user@test.local", "user_test", "Usuário Teste");
            var expectedId = UUID.randomUUID();
            setProfileId(profile, expectedId);

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            var result = service.currentUserId();

            assertThat(result).isEqualTo(expectedId);
            verify(userProfileService).getOrCreateCurrentUserProfile();
        }

        @Test
        void shouldCreateAndReturnIdForNewUser() {
            var profile = new UserProfile("sub-new", "new@test.local", "new_user", "Novo Usuário");

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            var result = service.currentUserId();

            assertThat(result).isEqualTo(profile.getId());
            verify(userProfileService).getOrCreateCurrentUserProfile();
        }
    }

    private static void setProfileId(UserProfile profile, UUID id) {
        try {
            Field field = UserProfile.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(profile, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Could not set UserProfile id for test", e);
        }
    }
}
