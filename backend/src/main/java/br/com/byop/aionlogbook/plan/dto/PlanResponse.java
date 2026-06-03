package br.com.byop.aionlogbook.plan.dto;

import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        UUID directionId,
        String title,
        String description,
        String type,
        Priority priority,
        PlanStatus status,
        LocalDate plannedDate,
        Instant plannedStartAt,
        Instant plannedEndAt,
        Integer estimatedMinutes,
        boolean notificationEnabled,
        Instant notificationDateTime,
        Instant startedAt,
        Instant finishedAt,
        Integer actualMinutes,
        String reason,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt,
        Instant lastStatusChangedAt
) {
}