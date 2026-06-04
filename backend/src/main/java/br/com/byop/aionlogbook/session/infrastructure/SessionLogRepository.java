package br.com.byop.aionlogbook.session.infrastructure;

import br.com.byop.aionlogbook.dashboard.dto.DashboardSessionProjection;
import br.com.byop.aionlogbook.session.domain.SessionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionLogRepository extends JpaRepository<SessionLog, UUID> {

    Optional<SessionLog> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Query("""
            select sessionLog
            from SessionLog sessionLog
            where sessionLog.userId = :userId
              and (:directionId is null or sessionLog.directionId = :directionId)
              and (:planId is null or sessionLog.planId = :planId)
              and sessionLog.startedAt between :dateFrom and :dateTo
            """)
    Page<SessionLog> findByFilters(
            UUID userId,
            UUID directionId,
            UUID planId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Pageable pageable
    );

    @Query("""
        select coalesce(sum(session.durationMinutes), 0)
        from SessionLog session
        where session.userId = :userId
          and session.startedAt >= :start
          and session.startedAt < :end
        """)
    Integer sumDurationMinutesByUserIdBetween(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    @Query("""
        select max(session.startedAt)
        from SessionLog session
        where session.userId = :userId
        """)
    OffsetDateTime findLastActivity(UUID userId);

    @Query("""
        select coalesce(sum(session.durationMinutes), 0)
        from SessionLog session
        where session.userId = :userId
        """)
    Integer sumTotalDurationMinutesByUserId(UUID userId);

    @Query("""
        select
            session.id as id,
            session.planId as planId,
            session.directionId as directionId,
            plan.title as planTitle,
            direction.name as directionName,
            session.durationMinutes as durationMinutes,
            session.startedAt as startedAt,
            session.finishedAt as finishedAt,
            session.createdAt as createdAt
        from SessionLog session
        left join Plan plan
            on plan.id = session.planId
           and plan.userId = :userId
        left join Direction direction
            on direction.id = session.directionId
           and direction.userProfile.id = :userId
        where session.userId = :userId
        order by session.startedAt desc
        """)
    List<DashboardSessionProjection> findLastDashboardSessions(
            UUID userId,
            Pageable pageable
    );
}
