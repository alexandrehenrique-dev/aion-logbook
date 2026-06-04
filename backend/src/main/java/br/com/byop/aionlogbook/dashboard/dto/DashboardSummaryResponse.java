package br.com.byop.aionlogbook.dashboard.dto;

import java.time.OffsetDateTime;

public record DashboardSummaryResponse(
        Integer totalTimeMinutes,
        Double completionRate,
        Long plansCreated,
        Long plansCompleted,
        Long activeDirections,
        Integer weeklyTimeMinutes,
        Integer streak,
        OffsetDateTime lastActivity
) {
}
