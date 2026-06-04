package br.com.byop.aionlogbook.session.application;

import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.session.domain.SessionLog;
import br.com.byop.aionlogbook.session.dto.CreateSessionLogRequest;
import br.com.byop.aionlogbook.session.dto.UpdateSessionLogRequest;
import br.com.byop.aionlogbook.session.infrastructure.SessionLogRepository;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionLogService {

    private static final OffsetDateTime DEFAULT_DATE_FROM =
            OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final OffsetDateTime DEFAULT_DATE_TO =
            OffsetDateTime.of(9999, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);

    private final SessionLogRepository sessionLogRepository;
    private final PlanRepository planRepository;
    private final DirectionRepository directionRepository;

    @Transactional(readOnly = true)
    public Page<SessionLog> findAll(
            UUID userId,
            UUID directionId,
            UUID planId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Pageable pageable
    ) {
        validateDirectionOwnership(userId, directionId);
        validatePlanOwnership(userId, planId);

        var normalizedDateFrom = dateFrom == null ? DEFAULT_DATE_FROM : dateFrom;
        var normalizedDateTo = dateTo == null ? DEFAULT_DATE_TO : dateTo;

        return sessionLogRepository.findByFilters(
                userId,
                directionId,
                planId,
                normalizedDateFrom,
                normalizedDateTo,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public SessionLog findById(UUID userId, UUID id) {
        return findByIdInternal(userId, id);
    }

    @Transactional
    public SessionLog create(UUID userId, CreateSessionLogRequest request) {
        validateDirectionOwnership(userId, request.directionId());
        validatePlanOwnership(userId, request.planId());

        var durationMinutes = resolveDurationMinutes(
                request.startedAt(),
                request.finishedAt(),
                request.actualMinutes()
        );

        var sessionLog = SessionLog.builder()
                .userId(userId)
                .planId(request.planId())
                .directionId(request.directionId())
                .startedAt(request.startedAt())
                .finishedAt(request.finishedAt())
                .durationMinutes(durationMinutes)
                .result(request.result())
                .notes(request.notes())
                .build();

        return sessionLogRepository.save(sessionLog);
    }

    @Transactional
    public SessionLog update(UUID userId, UUID id, UpdateSessionLogRequest request) {
        var sessionLog = findByIdInternal(userId, id);

        validateDirectionOwnership(userId, request.directionId());
        validatePlanOwnership(userId, request.planId());

        var startedAt = request.startedAt() == null
                ? sessionLog.getStartedAt()
                : request.startedAt();

        var finishedAt = request.finishedAt();

        var durationMinutes = resolveDurationMinutes(
                startedAt,
                finishedAt,
                request.actualMinutes()
        );

        sessionLog.setPlanId(request.planId());
        sessionLog.setDirectionId(request.directionId());
        sessionLog.setStartedAt(startedAt);
        sessionLog.setFinishedAt(finishedAt);
        sessionLog.setDurationMinutes(durationMinutes);
        sessionLog.setResult(request.result());
        sessionLog.setNotes(request.notes());

        return sessionLogRepository.save(sessionLog);
    }

    @Transactional
    public SessionLog createAutomaticFromPlan(UUID userId, AutomaticSessionLogRequest request) {
        validatePlanOwnership(userId, request.planId());
        validateDirectionOwnership(userId, request.directionId());

        var durationMinutes = resolveDurationMinutes(
                request.startedAt(),
                request.finishedAt(),
                request.actualMinutes()
        );

        var sessionLog = SessionLog.builder()
                .userId(userId)
                .planId(request.planId())
                .directionId(request.directionId())
                .startedAt(request.startedAt())
                .finishedAt(request.finishedAt())
                .durationMinutes(durationMinutes)
                .result(request.result())
                .notes(request.notes())
                .build();

        return sessionLogRepository.save(sessionLog);
    }

    private SessionLog findByIdInternal(UUID userId, UUID id) {
        return sessionLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada."));
    }

    private Integer resolveDurationMinutes(
            OffsetDateTime startedAt,
            OffsetDateTime finishedAt,
            Integer actualMinutes
    ) {
        if (actualMinutes != null) {
            if (actualMinutes <= 0) {
                throw new IllegalArgumentException("A duração deve ser maior que zero.");
            }

            return actualMinutes;
        }

        if (startedAt == null) {
            throw new IllegalArgumentException("startedAt é obrigatório quando actualMinutes não for informado.");
        }

        if (finishedAt == null) {
            throw new IllegalArgumentException("finishedAt é obrigatório quando actualMinutes não for informado.");
        }

        var duration = Duration.between(startedAt, finishedAt).toMinutes();

        if (duration <= 0) {
            throw new IllegalArgumentException("A duração calculada deve ser maior que zero.");
        }

        return Math.toIntExact(duration);
    }

    private void validatePlanOwnership(UUID userId, UUID planId) {
        if (planId == null) {
            return;
        }

        if (!planRepository.existsByIdAndUserId(planId, userId)) {
            throw new ResourceNotFoundException("Plano não encontrado.");
        }
    }

    private void validateDirectionOwnership(UUID userId, UUID directionId) {
        if (directionId == null) {
            return;
        }

        if (!directionRepository.existsByIdAndUserProfileId(directionId, userId)) {
            throw new ResourceNotFoundException("Direção não encontrada.");
        }
    }
}
