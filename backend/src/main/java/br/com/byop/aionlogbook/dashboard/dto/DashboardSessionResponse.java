package br.com.byop.aionlogbook.dashboard.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DashboardSessionResponse(
        UUID id,
        UUID planId,
        UUID directionId,
        String planTitle,
        String directionName,
        Integer durationMinutes,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        OffsetDateTime createdAt
) {
}
