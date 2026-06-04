package br.com.byop.aionlogbook.dashboard.dto;

import java.time.Instant;
import java.util.UUID;

public record DashboardLogEntryResponse(
        UUID id,
        UUID directionId,
        UUID planId,
        String title,
        String type,
        Instant createdAt
) {
}