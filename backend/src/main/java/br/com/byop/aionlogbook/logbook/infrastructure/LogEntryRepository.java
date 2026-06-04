package br.com.byop.aionlogbook.logbook.infrastructure;

import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LogEntryRepository extends JpaRepository<LogEntry, UUID>, LogEntryRepositoryCustom {

    Optional<LogEntry> findByIdAndUserId(UUID id, UUID userId);
}
