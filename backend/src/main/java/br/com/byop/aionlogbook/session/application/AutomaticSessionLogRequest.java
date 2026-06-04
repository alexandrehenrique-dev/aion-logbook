package br.com.byop.aionlogbook.session.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AutomaticSessionLogRequest(
        UUID planId,
        UUID directionId,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        Integer actualMinutes,
        String result,
        String notes
) {}
