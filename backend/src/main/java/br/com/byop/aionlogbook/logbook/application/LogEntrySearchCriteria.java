package br.com.byop.aionlogbook.logbook.application;

import br.com.byop.aionlogbook.logbook.domain.LogEntryType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LogEntrySearchCriteria(
        LogEntryType type,
        UUID directionId,
        UUID planId,
        List<String> tags,
        Instant dateFrom,
        Instant dateTo,
        String q
) {}
