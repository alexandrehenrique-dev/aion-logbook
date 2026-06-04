package br.com.byop.aionlogbook.logbook.dto;

import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateLogEntryRequest(
        UUID directionId,
        UUID planId,

        @Size(max = 200)
        String title,

        @Size(max = 10000)
        String content,

        LogEntryType type,

        List<String> tags
) {
}
