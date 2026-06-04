package br.com.byop.aionlogbook.direction.dto;

import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Resumo agregado de uma direção: planos e sessões vinculados")
public record DirectionSummaryResponse(
        UUID id,
        String name,
        String description,
        String color,
        String icon,
        DirectionStatus status,
        String identityPhrase,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        long totalPlans,
        long completedPlans,
        long activePlans,
        long totalSessions,
        long totalSessionMinutes
) {
}
