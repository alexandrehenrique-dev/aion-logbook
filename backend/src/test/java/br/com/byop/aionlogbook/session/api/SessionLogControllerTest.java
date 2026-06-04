package br.com.byop.aionlogbook.session.api;

import br.com.byop.aionlogbook.security.CurrentUserService;
import br.com.byop.aionlogbook.session.application.SessionLogService;
import br.com.byop.aionlogbook.session.domain.SessionLog;
import br.com.byop.aionlogbook.session.dto.CreateSessionLogRequest;
import br.com.byop.aionlogbook.session.dto.SessionLogResponse;
import br.com.byop.aionlogbook.session.dto.UpdateSessionLogRequest;
import br.com.byop.aionlogbook.session.mapper.SessionLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SessionLogControllerTest {

    private final SessionLogService sessionLogService = mock(SessionLogService.class);
    private final SessionLogMapper sessionLogMapper = mock(SessionLogMapper.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);

    private final SessionLogController controller =
            new SessionLogController(
                    sessionLogService,
                    sessionLogMapper,
                    currentUserService
            );

    @Nested
    class FindAll {

        @Test
        @DisplayName("deve listar sessoes do usuario autenticado")
        void shouldFindAllSessionsFromAuthenticatedUser() {
            var userId = UUID.randomUUID();
            var directionId = UUID.randomUUID();
            var planId = UUID.randomUUID();
            var dateFrom = OffsetDateTime.now().minusDays(7);
            var dateTo = OffsetDateTime.now();
            var pageable = PageRequest.of(0, 10);

            var sessionLog = sessionLog(userId);
            var response = response(sessionLog);

            when(currentUserService.currentUserId()).thenReturn(userId);
            when(sessionLogService.findAll(userId, directionId, planId, dateFrom, dateTo, pageable))
                    .thenReturn(new PageImpl<>(List.of(sessionLog), pageable, 1));
            when(sessionLogMapper.toResponse(sessionLog)).thenReturn(response);

            var result = controller.findAll(directionId, planId, dateFrom, dateTo, pageable);

            assertThat(result.getContent()).containsExactly(response);

            verify(currentUserService).currentUserId();
            verify(sessionLogService).findAll(userId, directionId, planId, dateFrom, dateTo, pageable);
            verify(sessionLogMapper).toResponse(sessionLog);
        }
    }

    @Nested
    class Create {

        @Test
        @DisplayName("deve criar sessao manual para o usuario autenticado")
        void shouldCreateSessionForAuthenticatedUser() {
            var userId = UUID.randomUUID();
            var startedAt = OffsetDateTime.now();

            var request = new CreateSessionLogRequest(
                    null,
                    null,
                    startedAt,
                    null,
                    30,
                    "Resultado",
                    "Notas"
            );

            var sessionLog = sessionLog(userId);
            var response = response(sessionLog);

            when(currentUserService.currentUserId()).thenReturn(userId);
            when(sessionLogService.create(userId, request)).thenReturn(sessionLog);
            when(sessionLogMapper.toResponse(sessionLog)).thenReturn(response);

            var result = controller.create(request);

            assertThat(result).isEqualTo(response);

            verify(currentUserService).currentUserId();
            verify(sessionLogService).create(userId, request);
            verify(sessionLogMapper).toResponse(sessionLog);
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("deve buscar sessao por id usando usuario autenticado")
        void shouldFindSessionByIdUsingAuthenticatedUser() {
            var userId = UUID.randomUUID();
            var id = UUID.randomUUID();

            var sessionLog = sessionLog(userId);
            sessionLog.setId(id);

            var response = response(sessionLog);

            when(currentUserService.currentUserId()).thenReturn(userId);
            when(sessionLogService.findById(userId, id)).thenReturn(sessionLog);
            when(sessionLogMapper.toResponse(sessionLog)).thenReturn(response);

            var result = controller.findById(id);

            assertThat(result).isEqualTo(response);

            verify(currentUserService).currentUserId();
            verify(sessionLogService).findById(userId, id);
            verify(sessionLogMapper).toResponse(sessionLog);
        }
    }

    @Nested
    class Update {

        @Test
        @DisplayName("deve atualizar sessao usando usuario autenticado")
        void shouldUpdateSessionUsingAuthenticatedUser() {
            var userId = UUID.randomUUID();
            var id = UUID.randomUUID();

            var request = new UpdateSessionLogRequest(
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    45,
                    "Atualizado",
                    "Notas atualizadas"
            );

            var sessionLog = sessionLog(userId);
            sessionLog.setId(id);

            var response = response(sessionLog);

            when(currentUserService.currentUserId()).thenReturn(userId);
            when(sessionLogService.update(userId, id, request)).thenReturn(sessionLog);
            when(sessionLogMapper.toResponse(sessionLog)).thenReturn(response);

            var result = controller.update(id, request);

            assertThat(result).isEqualTo(response);

            verify(currentUserService).currentUserId();
            verify(sessionLogService).update(userId, id, request);
            verify(sessionLogMapper).toResponse(sessionLog);
        }
    }

    private SessionLog sessionLog(UUID userId) {
        var startedAt = OffsetDateTime.now();

        return SessionLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .startedAt(startedAt)
                .finishedAt(startedAt.plusMinutes(30))
                .durationMinutes(30)
                .result("Resultado")
                .notes("Notas")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private SessionLogResponse response(SessionLog sessionLog) {
        return new SessionLogResponse(
                sessionLog.getId(),
                sessionLog.getUserId(),
                sessionLog.getPlanId(),
                sessionLog.getDirectionId(),
                sessionLog.getStartedAt(),
                sessionLog.getFinishedAt(),
                sessionLog.getDurationMinutes(),
                sessionLog.getResult(),
                sessionLog.getNotes(),
                sessionLog.getCreatedAt()
        );
    }
}