package br.com.byop.aionlogbook.settings.application;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.settings.dto.SettingsResponse;
import br.com.byop.aionlogbook.settings.dto.UpdateSettingsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Set;

@Service
public class SettingsService {

    private static final Set<String> ALLOWED_THEMES = Set.of("system", "light", "dark");

    private final UserProfileService userProfileService;

    public SettingsService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Transactional(readOnly = true)
    public SettingsResponse getCurrentSettings() {
        UserProfile profile = userProfileService.getOrCreateCurrentUserProfile();
        return toResponse(profile);
    }

    @Transactional
    public SettingsResponse update(UpdateSettingsRequest request) {
        UserProfile profile = userProfileService.getOrCreateCurrentUserProfile();

        validateTimezone(request.timezone());
        validateTheme(request.theme());

        String timezone = request.timezone() != null
                ? request.timezone()
                : profile.getTimezone();

        String theme = request.theme() != null
                ? request.theme()
                : profile.getTheme();

        Integer defaultPlanDuration = request.defaultPlanDuration() != null
                ? request.defaultPlanDuration()
                : profile.getDefaultPlanDuration();

        Boolean notificationsEnabled = request.notificationsEnabled() != null
                ? request.notificationsEnabled()
                : profile.isNotificationsEnabled();

        Integer notificationLeadMinutes = request.notificationLeadMinutes() != null
                ? request.notificationLeadMinutes()
                : profile.getNotificationLeadMinutes();

        profile.updateSettings(
                timezone,
                theme,
                defaultPlanDuration,
                notificationsEnabled,
                notificationLeadMinutes
        );

        return toResponse(profile);
    }

    private void validateTimezone(String timezone) {
        if (timezone == null) {
            return;
        }

        try {
            ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("timezone deve ser um identificador IANA válido");
        }
    }

    private void validateTheme(String theme) {
        if (theme == null) {
            return;
        }

        if (!ALLOWED_THEMES.contains(theme)) {
            throw new IllegalArgumentException("theme deve ser system, light ou dark");
        }
    }

    private SettingsResponse toResponse(UserProfile profile) {
        return new SettingsResponse(
                profile.getTimezone(),
                profile.getTheme(),
                profile.getDefaultPlanDuration(),
                profile.isNotificationsEnabled(),
                profile.getNotificationLeadMinutes(),
                profile.isOnboardingCompleted()
        );
    }
}