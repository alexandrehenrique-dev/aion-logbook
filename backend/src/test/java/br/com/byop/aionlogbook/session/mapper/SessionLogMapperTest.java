package br.com.byop.aionlogbook.session.mapper;

import br.com.byop.aionlogbook.session.domain.SessionLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessionLogMapperTest {

    private final SessionLogMapper mapper = new SessionLogMapper();

    @Test
    @DisplayName("deve converter SessionLog para SessionLogResponse")
    void shouldMapSessionLogToResponse() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var startedAt = OffsetDateTime.now();
        var finishedAt = startedAt.plusMinutes(45);
        var createdAt = OffsetDateTime.now();

        var sessionLog = SessionLog.builder()
                .id(id)
                .userId(userId)
                .planId(planId)
                .directionId(directionId)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .durationMinutes(45)
                .result("Sessão concluída")
                .notes("Notas da sessão")
                .createdAt(createdAt)
                .build();

        var response = mapper.toResponse(sessionLog);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.planId()).isEqualTo(planId);
        assertThat(response.directionId()).isEqualTo(directionId);
        assertThat(response.startedAt()).isEqualTo(startedAt);
        assertThat(response.finishedAt()).isEqualTo(finishedAt);
        assertThat(response.durationMinutes()).isEqualTo(45);
        assertThat(response.result()).isEqualTo("Sessão concluída");
        assertThat(response.notes()).isEqualTo("Notas da sessão");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}