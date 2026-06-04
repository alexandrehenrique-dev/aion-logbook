package br.com.byop.aionlogbook.session.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateSessionLogRequest(

        UUID planId,

        UUID directionId,

        @NotNull
        OffsetDateTime startedAt,

        OffsetDateTime finishedAt,

        @Min(1)
        Integer actualMinutes,

        @Size(max = 1000)
        String result,

        String notes
) {
}
