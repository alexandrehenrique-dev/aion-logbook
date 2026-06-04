package br.com.byop.aionlogbook.dashboard.dto;

import br.com.byop.aionlogbook.plan.domain.PlanStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DashboardPlanResponse(
        UUID id,
        UUID directionId,
        String title,
        PlanStatus status,
        LocalDate plannedDate,
        Instant plannedStartAt,
        Instant startedAt,
        Instant finishedAt,
        Integer estimatedMinutes,
        Integer actualMinutes
) {
}