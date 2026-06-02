package br.com.byop.aionlogbook.direction.mapper;

import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.dto.DirectionResponse;

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
}
