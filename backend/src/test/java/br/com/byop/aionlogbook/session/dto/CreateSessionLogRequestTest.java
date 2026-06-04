package br.com.byop.aionlogbook.session.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSessionLogRequestTest {

    @Test
    @DisplayName("deve criar request de criacao com campos esperados")
    void shouldCreateRequest() {
        var planId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var startedAt = OffsetDateTime.now();
        var finishedAt = startedAt.plusMinutes(25);

        var request = new CreateSessionLogRequest(
                planId,
                directionId,
                startedAt,
                finishedAt,
                25,
                "Sessão concluída",
                "Notas da sessão"
        );

        assertThat(request.planId()).isEqualTo(planId);
        assertThat(request.directionId()).isEqualTo(directionId);
        assertThat(request.startedAt()).isEqualTo(startedAt);
        assertThat(request.finishedAt()).isEqualTo(finishedAt);
        assertThat(request.actualMinutes()).isEqualTo(25);
        assertThat(request.result()).isEqualTo("Sessão concluída");
        assertThat(request.notes()).isEqualTo("Notas da sessão");
    }
}