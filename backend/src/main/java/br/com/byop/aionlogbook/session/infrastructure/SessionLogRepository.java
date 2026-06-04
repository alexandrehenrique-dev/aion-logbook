package br.com.byop.aionlogbook.session.infrastructure;

import br.com.byop.aionlogbook.session.domain.SessionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
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
}
