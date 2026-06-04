package br.com.byop.aionlogbook.analytics.domain;

import java.time.LocalDate;

public record PlannedVsExecutedProjection(
        LocalDate date,
        long plannedMinutes,
        long executedMinutes
) {
}
