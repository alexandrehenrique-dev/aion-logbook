package br.com.byop.aionlogbook.analytics.domain;

public record AnalyticsOverviewProjection(
        long plansCreated,
        long plansCompleted,
        long plansPartial,
        long plansMissed,
        long plansIgnored,
        long plansCanceled,
        long activeDirections,
        long totalTimeMinutes,
        long weeklyTimeMinutes
) {
}