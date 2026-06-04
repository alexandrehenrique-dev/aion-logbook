package br.com.byop.aionlogbook.logbook.dto;

import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateLogEntryRequest(
        UUID directionId,
        UUID planId,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 10000)
        String content,

        @NotNull
        LogEntryType type,

        List<String> tags
) {
}
