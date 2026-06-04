package br.com.byop.aionlogbook.logbook.dto;

import br.com.byop.aionlogbook.logbook.domain.LogEntryType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LogEntryResponse(
        UUID id,
        UUID directionId,
        UUID planId,
        String title,
        String content,
        LogEntryType type,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
