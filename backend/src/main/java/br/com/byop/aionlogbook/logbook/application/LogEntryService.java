package br.com.byop.aionlogbook.logbook.application;

import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import br.com.byop.aionlogbook.logbook.dto.*;
import br.com.byop.aionlogbook.logbook.infrastructure.LogEntryRepository;
import br.com.byop.aionlogbook.logbook.mapper.LogEntryMapper;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;
    private final DirectionRepository directionRepository;
    private final PlanRepository planRepository;
    private final LogEntryMapper logEntryMapper;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public LogEntryService(
            LogEntryRepository logEntryRepository,
            DirectionRepository directionRepository,
            PlanRepository planRepository,
            LogEntryMapper logEntryMapper,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.logEntryRepository = logEntryRepository;
        this.directionRepository = directionRepository;
        this.planRepository = planRepository;
        this.logEntryMapper = logEntryMapper;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<LogEntryResponse> list(
            UUID userId,
            LogEntryType type,
            UUID directionId,
            UUID planId,
            List<String> tags,
            Instant dateFrom,
            Instant dateTo,
            String q,
            Pageable pageable
    ) {
        validateOwnership(userId, directionId, planId);

        return logEntryRepository.findByFilters(
                userId,
                type == null ? null : type.name(),
                directionId,
                planId,
                dateFrom,
                dateTo,
                toTagsJson(tags),
                normalize(q),
                pageable
        ).map(logEntryMapper::toResponse);
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

    private br.com.byop.aionlogbook.logbook.domain.LogEntry findOwned(UUID userId, UUID id) {
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

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String toTagsJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid tags filter");
        }
    }
}
