package br.com.byop.aionlogbook.analytics.dto;

public record AnalyticsOverviewResponse(
        long totalPlans,
        long completedPlans,
        long partialPlans,
        long missedPlans,
        long ignoredPlans,
        long canceledPlans,
        long activeDirections,
        long executedMinutes,
        long weeklyTimeMinutes,
        double completionRate
) {}