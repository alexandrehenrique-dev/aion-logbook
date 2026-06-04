package br.com.byop.aionlogbook.settings.application;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.settings.dto.SettingsResponse;
import br.com.byop.aionlogbook.settings.dto.UpdateSettingsRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private SettingsService service;

    @Nested
    class GetCurrentSettings {

        @Test
        void shouldReturnCurrentUserProfileSettings() {
            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            SettingsResponse response = service.getCurrentSettings();

            assertThat(response.timezone()).isEqualTo("America/Sao_Paulo");
            assertThat(response.theme()).isEqualTo("system");
            assertThat(response.defaultPlanDuration()).isEqualTo(60);
            assertThat(response.notificationsEnabled()).isTrue();
            assertThat(response.notificationLeadMinutes()).isEqualTo(10);
            assertThat(response.onboardingCompleted()).isFalse();
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateSettings() {
            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            UpdateSettingsRequest request = new UpdateSettingsRequest(
                    "UTC",
                    "dark",
                    120,
                    false,
                    30
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            SettingsResponse response = service.update(request);

            assertThat(response.timezone()).isEqualTo("UTC");
            assertThat(response.theme()).isEqualTo("dark");
            assertThat(response.defaultPlanDuration()).isEqualTo(120);
            assertThat(response.notificationsEnabled()).isFalse();
            assertThat(response.notificationLeadMinutes()).isEqualTo(30);
            assertThat(profile.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldKeepCurrentValuesWhenRequestFieldsAreNull() {
            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            profile.updateSettings(
                    "UTC",
                    "light",
                    90,
                    false,
                    20
            );

            UpdateSettingsRequest request = new UpdateSettingsRequest(
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            SettingsResponse response = service.update(request);

            assertThat(response.timezone()).isEqualTo("UTC");
            assertThat(response.theme()).isEqualTo("light");
            assertThat(response.defaultPlanDuration()).isEqualTo(90);
            assertThat(response.notificationsEnabled()).isFalse();
            assertThat(response.notificationLeadMinutes()).isEqualTo(20);
        }

        @Test
        void shouldThrowExceptionWhenTimezoneIsInvalid() {
            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            UpdateSettingsRequest request = new UpdateSettingsRequest(
                    "Terra/Media",
                    "dark",
                    60,
                    true,
                    10
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            assertThatThrownBy(() -> service.update(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("timezone deve ser um identificador IANA válido");
        }

        @Test
        void shouldThrowExceptionWhenThemeIsInvalid() {
            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            UpdateSettingsRequest request = new UpdateSettingsRequest(
                    "UTC",
                    "matrix",
                    60,
                    true,
                    10
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            assertThatThrownBy(() -> service.update(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("theme deve ser system, light ou dark");
        }
    }
}