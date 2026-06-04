package br.com.byop.aionlogbook.analytics.dto;

import java.time.LocalDate;

public record PlannedVsExecutedResponse(
        String day,
        LocalDate date,
        long plannedMinutes,
        long executedMinutes
) {}