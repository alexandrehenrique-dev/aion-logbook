package br.com.byop.aionlogbook.analytics.dto;

import java.util.UUID;

public record TimeByDirectionResponse(
        UUID directionId,
        String directionName,
        String color,
        long totalMinutes,
        double percentage,
        long sessionsCount
) {
}