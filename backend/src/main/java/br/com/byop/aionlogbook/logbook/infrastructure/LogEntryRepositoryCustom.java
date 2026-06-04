package br.com.byop.aionlogbook.logbook.infrastructure;

import br.com.byop.aionlogbook.logbook.application.LogEntrySearchCriteria;
import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LogEntryRepositoryCustom {

    Page<LogEntry> findByFilters(UUID userId, LogEntrySearchCriteria criteria, Pageable pageable);
}
