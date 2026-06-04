package br.com.byop.aionlogbook.direction.mapper;

import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.dto.DirectionResponse;
import br.com.byop.aionlogbook.direction.dto.DirectionSummaryResponse;

public final class DirectionMapper {

    private DirectionMapper() {
    }

    public static DirectionResponse toResponse(Direction direction) {
        return new DirectionResponse(
                direction.getId(),
                direction.getName(),
                direction.getDescription(),
                direction.getColor(),
                direction.getIcon(),
                direction.getStatus(),
                direction.getIdentityPhrase(),
                direction.getArchivedAt(),
                direction.getCreatedAt(),
                direction.getUpdatedAt()
        );
    }

    public static DirectionSummaryResponse toSummaryResponse(
            Direction direction,
            long totalPlans,
            long completedPlans,
            long activePlans,
            long totalSessions,
            long totalSessionMinutes
    ) {
        return new DirectionSummaryResponse(
                direction.getId(),
                direction.getName(),
                direction.getDescription(),
                direction.getColor(),
                direction.getIcon(),
                direction.getStatus(),
                direction.getIdentityPhrase(),
                direction.getCreatedAt(),
                direction.getUpdatedAt(),
                totalPlans,
                completedPlans,
                activePlans,
                totalSessions,
                totalSessionMinutes
        );
    }
}
