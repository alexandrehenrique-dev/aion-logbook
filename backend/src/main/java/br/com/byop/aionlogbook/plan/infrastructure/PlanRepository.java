package br.com.byop.aionlogbook.plan.infrastructure;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
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

    boolean existsByUserIdAndStatusAndIdNot(
            UUID userId,
            PlanStatus status,
            UUID ignoredPlanId
    );

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select plan
        from Plan plan
        where plan.status in (:statuses)
          and plan.plannedStartAt is not null
          and plan.plannedStartAt <= :now
        order by plan.plannedStartAt asc
        """)
    List<Plan> findDueCandidates(
            @Param("statuses") Collection<PlanStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select plan
        from Plan plan
        where plan.status = :status
          and plan.plannedEndAt is not null
          and plan.plannedEndAt < :now
        order by plan.plannedEndAt asc
        """)
    List<Plan> findMissedCandidates(
            @Param("status") PlanStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );
}
