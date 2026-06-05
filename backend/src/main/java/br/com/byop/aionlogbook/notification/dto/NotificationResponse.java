package br.com.byop.aionlogbook.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String description,
        String type,
        boolean unread,
        UUID relatedEntityId,
        Instant createdAt
) {
}
