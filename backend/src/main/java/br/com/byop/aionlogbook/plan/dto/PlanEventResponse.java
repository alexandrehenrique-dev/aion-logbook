package br.com.byop.aionlogbook.plan.dto;

import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PlanEventResponse(
        UUID id,
        UUID planId,
        PlanEventType eventType,
        PlanStatus fromStatus,
        PlanStatus toStatus,
        String description,
        Map<String, Object> metadata,
        Instant createdAt
) {
}