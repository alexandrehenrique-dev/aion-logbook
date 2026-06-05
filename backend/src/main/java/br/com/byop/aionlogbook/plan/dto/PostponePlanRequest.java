package br.com.byop.aionlogbook.plan.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

public record PostponePlanRequest(
        LocalDate plannedDate,
        @NotNull Instant plannedStartAt,
        Integer estimatedMinutes,
        String reason
) {}
