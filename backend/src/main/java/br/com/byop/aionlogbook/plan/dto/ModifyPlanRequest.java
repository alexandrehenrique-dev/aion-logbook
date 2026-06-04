package br.com.byop.aionlogbook.plan.dto;

import br.com.byop.aionlogbook.plan.domain.Priority;

import java.time.Instant;
import java.time.LocalDate;

public record ModifyPlanRequest(
        String title,
        String description,
        String type,
        Priority priority,
        LocalDate plannedDate,
        Instant plannedStartAt,
        Integer estimatedMinutes,
        Boolean notificationEnabled,
        Instant notificationDateTime,
        String reason
) {}
