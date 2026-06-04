package br.com.byop.aionlogbook.session.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SessionLogResponseTest {

    @Test
    @DisplayName("deve criar response com campos esperados")
    void shouldCreateResponse() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var startedAt = OffsetDateTime.now();
        var finishedAt = startedAt.plusMinutes(30);
        var createdAt = OffsetDateTime.now();

        var response = new SessionLogResponse(
                id,
                userId,
                planId,
                directionId,
                startedAt,
                finishedAt,
                30,
                "Sessão concluída",
                "Notas",
                createdAt
        );

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.planId()).isEqualTo(planId);
        assertThat(response.directionId()).isEqualTo(directionId);
        assertThat(response.startedAt()).isEqualTo(startedAt);
        assertThat(response.finishedAt()).isEqualTo(finishedAt);
        assertThat(response.durationMinutes()).isEqualTo(30);
        assertThat(response.result()).isEqualTo("Sessão concluída");
        assertThat(response.notes()).isEqualTo("Notas");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}