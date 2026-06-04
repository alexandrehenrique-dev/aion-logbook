package br.com.byop.aionlogbook.logbook.mapper;

import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import br.com.byop.aionlogbook.logbook.dto.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class LogEntryMapper {

    public LogEntry toEntity(CreateLogEntryRequest request, UUID userId, Instant now) {
        var entry = new LogEntry();

        entry.setId(UUID.randomUUID());
        entry.setUserId(userId);
        entry.setDirectionId(request.directionId());
        entry.setPlanId(request.planId());
        entry.setTitle(request.title());
        entry.setContent(request.content());
        entry.setType(request.type());
        entry.setTags(request.tags());
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);

        return entry;
    }

    public void applyUpdate(LogEntry entry, UpdateLogEntryRequest request, Instant now) {
        if (request.directionId() != null) {
            entry.setDirectionId(request.directionId());
        }

        if (request.planId() != null) {
            entry.setPlanId(request.planId());
        }

        if (request.title() != null) {
            entry.setTitle(request.title());
        }

        if (request.content() != null) {
            entry.setContent(request.content());
        }

        if (request.type() != null) {
            entry.setType(request.type());
        }

        if (request.tags() != null) {
            entry.setTags(request.tags());
        }

        entry.setUpdatedAt(now);
    }

    public LogEntryResponse toResponse(LogEntry entry) {
        return new LogEntryResponse(
                entry.getId(),
                entry.getDirectionId(),
                entry.getPlanId(),
                entry.getTitle(),
                entry.getContent(),
                entry.getType(),
                entry.getTags(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}
