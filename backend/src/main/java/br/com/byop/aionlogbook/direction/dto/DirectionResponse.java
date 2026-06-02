package br.com.byop.aionlogbook.direction.dto;

import br.com.byop.aionlogbook.direction.domain.DirectionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DirectionResponse(
        UUID id,
        String name,
        String description,
        String color,
        String icon,
        DirectionStatus status,
        String identityPhrase,
        OffsetDateTime archivedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
