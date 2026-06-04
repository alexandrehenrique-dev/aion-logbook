package br.com.byop.aionlogbook.session.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionLogResponse(

        UUID id,

        UUID userId,

        UUID planId,

        UUID directionId,

        OffsetDateTime startedAt,

        OffsetDateTime finishedAt,

        Integer durationMinutes,

        String result,

        String notes,

        OffsetDateTime createdAt
) {
}