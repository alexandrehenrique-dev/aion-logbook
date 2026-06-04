package br.com.byop.aionlogbook.dashboard.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface DashboardSessionProjection {

    UUID getId();

    UUID getPlanId();

    UUID getDirectionId();

    String getPlanTitle();

    String getDirectionName();

    Integer getDurationMinutes();

    OffsetDateTime getStartedAt();

    OffsetDateTime getFinishedAt();

    OffsetDateTime getCreatedAt();
}
