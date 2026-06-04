package br.com.byop.aionlogbook.settings.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateSettingsRequest(
        String timezone,
        String theme,

        @Min(5)
        @Max(480)
        Integer defaultPlanDuration,

        Boolean notificationsEnabled,

        @Min(0)
        @Max(1440)
        Integer notificationLeadMinutes
) {
}