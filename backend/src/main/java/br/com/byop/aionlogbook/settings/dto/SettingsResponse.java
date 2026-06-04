package br.com.byop.aionlogbook.settings.dto;

public record SettingsResponse(
        String timezone,
        String theme,
        Integer defaultPlanDuration,
        Boolean notificationsEnabled,
        Integer notificationLeadMinutes,
        Boolean onboardingCompleted
) {
}