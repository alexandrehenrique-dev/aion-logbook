package br.com.byop.aionlogbook.direction.infrastructure;

import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DirectionRepository extends JpaRepository<Direction, UUID> {

    List<Direction> findAllByUserProfileIdAndStatusOrderByNameAsc(
            UUID userProfileId,
            DirectionStatus status
    );

    List<Direction> findAllByUserProfileIdOrderByNameAsc(UUID userProfileId);

    Optional<Direction> findByIdAndUserProfileId(UUID id, UUID userProfileId);

    boolean existsByIdAndUserProfileIdAndStatus(UUID id, UUID userProfileId, DirectionStatus status);

    boolean existsByIdAndUserProfileId(UUID id, UUID userProfileId);

    long countByUserProfileIdAndStatus(UUID userProfileId, DirectionStatus status);
}
