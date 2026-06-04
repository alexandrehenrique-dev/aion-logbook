package br.com.byop.aionlogbook.logbook.infrastructure;

import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LogEntryRepository extends JpaRepository<LogEntry, UUID> {

    Optional<LogEntry> findByIdAndUserId(UUID id, UUID userId);

    @Query(
            value = """
                select *
                from log_entries log
                where log.user_id = :userId
                  and (cast(:type as varchar) is null or log.type = :type)
                  and (cast(:directionId as uuid) is null or log.direction_id = :directionId)
                  and (cast(:planId as uuid) is null or log.plan_id = :planId)
                  and (cast(:dateFrom as timestamptz) is null or log.created_at >= :dateFrom)
                  and (cast(:dateTo as timestamptz) is null or log.created_at <= :dateTo)
                  and (cast(:tagsJson as jsonb) is null or log.tags @> cast(:tagsJson as jsonb))
                  and (
                        cast(:q as varchar) is null
                        or lower(log.title) like lower(concat('%', :q, '%'))
                        or lower(log.content) like lower(concat('%', :q, '%'))
                  )
                order by log.created_at desc
                """,
            countQuery = """
                select count(*)
                from log_entries log
                where log.user_id = :userId
                  and (cast(:type as varchar) is null or log.type = :type)
                  and (cast(:directionId as uuid) is null or log.direction_id = :directionId)
                  and (cast(:planId as uuid) is null or log.plan_id = :planId)
                  and (cast(:dateFrom as timestamptz) is null or log.created_at >= :dateFrom)
                  and (cast(:dateTo as timestamptz) is null or log.created_at <= :dateTo)
                  and (cast(:tagsJson as jsonb) is null or log.tags @> cast(:tagsJson as jsonb))
                  and (
                        cast(:q as varchar) is null
                        or lower(log.title) like lower(concat('%', :q, '%'))
                        or lower(log.content) like lower(concat('%', :q, '%'))
                  )
                """,
            nativeQuery = true
    )
    Page<LogEntry> findByFilters(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("directionId") UUID directionId,
            @Param("planId") UUID planId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("tagsJson") String tagsJson,
            @Param("q") String q,
            Pageable pageable
    );
}
