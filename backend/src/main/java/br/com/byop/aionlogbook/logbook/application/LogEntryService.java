package br.com.byop.aionlogbook.logbook.application;

import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import br.com.byop.aionlogbook.logbook.dto.*;
import br.com.byop.aionlogbook.logbook.infrastructure.LogEntryRepository;
import br.com.byop.aionlogbook.logbook.mapper.LogEntryMapper;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;
    private final DirectionRepository directionRepository;
    private final PlanRepository planRepository;
    private final LogEntryMapper logEntryMapper;
    private final Clock clock;

    public LogEntryService(
            LogEntryRepository logEntryRepository,
            DirectionRepository directionRepository,
            PlanRepository planRepository,
            LogEntryMapper logEntryMapper,
            Clock clock
    ) {
        this.logEntryRepository = logEntryRepository;
        this.directionRepository = directionRepository;
        this.planRepository = planRepository;
        this.logEntryMapper = logEntryMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<LogEntryResponse> list(UUID userId, LogEntrySearchCriteria criteria, Pageable pageable) {
        validateOwnership(userId, criteria.directionId(), criteria.planId());

        return logEntryRepository.findByFilters(userId, criteria, pageable)
                .map(logEntryMapper::toResponse);
    }

    @Transactional
    public LogEntryResponse create(UUID userId, CreateLogEntryRequest request) {
        validateOwnership(userId, request.directionId(), request.planId());

        var entry = logEntryMapper.toEntity(request, userId, Instant.now(clock));
        var saved = logEntryRepository.save(entry);

        return logEntryMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LogEntryResponse getById(UUID userId, UUID id) {
        return logEntryMapper.toResponse(findOwned(userId, id));
    }

    @Transactional
    public LogEntryResponse update(UUID userId, UUID id, UpdateLogEntryRequest request) {
        var entry = findOwned(userId, id);

        validateOwnership(userId, request.directionId(), request.planId());

        logEntryMapper.applyUpdate(entry, request, Instant.now(clock));

        return logEntryMapper.toResponse(logEntryRepository.save(entry));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        var entry = findOwned(userId, id);

        logEntryRepository.delete(entry);
    }

    private LogEntry findOwned(UUID userId, UUID id) {
        return logEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Log entry not found"));
    }

    private void validateOwnership(UUID userId, UUID directionId, UUID planId) {
        if (directionId != null && !directionRepository.existsByIdAndUserProfileId(directionId, userId)) {
            throw new ResourceNotFoundException("Direction not found");
        }

        if (planId != null && !planRepository.existsByIdAndUserId(planId, userId)) {
            throw new ResourceNotFoundException("Plan not found");
        }
    }
}
