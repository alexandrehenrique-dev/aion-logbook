package br.com.byop.aionlogbook.plan.dto;

import br.com.byop.aionlogbook.plan.domain.Priority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePlanRequest(
        UUID directionId,

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 1000)
        String description,

        @Size(max = 100)
        String type,

        Priority priority,

        LocalDate plannedDate,

        Instant plannedStartAt,

        @Min(1)
        Integer estimatedMinutes,

        boolean notificationEnabled,

        Instant notificationDateTime,

        @Size(max = 1000)
        String reason,

        List<String> tags
) {
}