package br.com.byop.aionlogbook.analytics.domain;

import java.util.UUID;

public record TimeByDirectionProjection(
        UUID directionId,
        String directionName,
        String color,
        long totalMinutes,
        long sessionsCount
) {
}