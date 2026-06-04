package br.com.byop.aionlogbook.logbook.mapper;

import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import br.com.byop.aionlogbook.logbook.dto.CreateLogEntryRequest;
import br.com.byop.aionlogbook.logbook.dto.UpdateLogEntryRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogEntryMapperTest {

    private final LogEntryMapper mapper = new LogEntryMapper();

    @Test
    void shouldMapCreateRequestToEntity() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var now = Instant.parse("2026-06-04T10:00:00Z");

        var request = new CreateLogEntryRequest(
                directionId,
                planId,
                "Título",
                "Conteúdo",
                LogEntryType.REFLECTION,
                List.of("tag")
        );

        var entity = mapper.toEntity(request, userId, now);

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getDirectionId()).isEqualTo(directionId);
        assertThat(entity.getPlanId()).isEqualTo(planId);
        assertThat(entity.getTitle()).isEqualTo("Título");
        assertThat(entity.getContent()).isEqualTo("Conteúdo");
        assertThat(entity.getType()).isEqualTo(LogEntryType.REFLECTION);
        assertThat(entity.getTags()).containsExactly("tag");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldApplyOnlyProvidedUpdateFields() {
        var entry = new LogEntry();
        entry.setTitle("Antigo");
        entry.setContent("Conteúdo antigo");
        entry.setType(LogEntryType.IDEA);

        var now = Instant.parse("2026-06-04T11:00:00Z");

        var request = new UpdateLogEntryRequest(
                null,
                null,
                "Novo",
                null,
                LogEntryType.DECISION,
                null
        );

        mapper.applyUpdate(entry, request, now);

        assertThat(entry.getTitle()).isEqualTo("Novo");
        assertThat(entry.getContent()).isEqualTo("Conteúdo antigo");
        assertThat(entry.getType()).isEqualTo(LogEntryType.DECISION);
        assertThat(entry.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapEntityToResponse() {
        var id = UUID.randomUUID();

        var entry = new LogEntry();
        entry.setId(id);
        entry.setTitle("Título");
        entry.setContent("Conteúdo");
        entry.setType(LogEntryType.SYNTHESIS);
        entry.setTags(List.of("aion"));

        var response = mapper.toResponse(entry);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.title()).isEqualTo("Título");
        assertThat(response.content()).isEqualTo("Conteúdo");
        assertThat(response.type()).isEqualTo(LogEntryType.SYNTHESIS);
        assertThat(response.tags()).containsExactly("aion");
    }
}
