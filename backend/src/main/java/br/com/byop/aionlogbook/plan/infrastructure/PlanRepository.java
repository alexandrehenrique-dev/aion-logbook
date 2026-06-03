package br.com.byop.aionlogbook.plan.infrastructure;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Optional<Plan> findByIdAndUserId(UUID id, UUID userId);

    Page<Plan> findByUserId(UUID userId, Pageable pageable);

    Page<Plan> findByUserIdAndStatus(UUID userId, PlanStatus status, Pageable pageable);

    Page<Plan> findByUserIdAndDirectionId(UUID userId, UUID directionId, Pageable pageable);

    Page<Plan> findByUserIdAndPlannedDate(UUID userId, LocalDate plannedDate, Pageable pageable);

    Page<Plan> findByUserIdAndStatusAndDirectionIdAndPlannedDate(
            UUID userId,
            PlanStatus status,
            UUID directionId,
            LocalDate plannedDate,
            Pageable pageable
    );

    @Query("""
            select plan
            from Plan plan
            where plan.userId = :userId
              and (:status is null or plan.status = :status)
              and (:directionId is null or plan.directionId = :directionId)
              and (:plannedDate is null or plan.plannedDate = :plannedDate)
            """)
    Page<Plan> findByFilters(
            UUID userId,
            PlanStatus status,
            UUID directionId,
            LocalDate plannedDate,
            Pageable pageable
    );
}
