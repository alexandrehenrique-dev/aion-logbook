package br.com.byop.aionlogbook.plan.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record PostponePlanRequest(
        @NotNull Instant plannedStartAt,
        Integer estimatedMinutes,
        String reason
) {}
