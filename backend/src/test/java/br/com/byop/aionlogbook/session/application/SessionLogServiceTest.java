package br.com.byop.aionlogbook.session.application;

import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.session.domain.SessionLog;
import br.com.byop.aionlogbook.session.dto.CreateSessionLogRequest;
import br.com.byop.aionlogbook.session.infrastructure.SessionLogRepository;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionLogServiceTest {

    @Mock
    private SessionLogRepository sessionLogRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private DirectionRepository directionRepository;

    @InjectMocks
    private SessionLogService service;

    @Nested
    class FindAll {

        @Test
        @DisplayName("deve aplicar filtros combinados de direção e plano")
        void shouldApplyCombinedDirectionAndPlanFilters() {
            var userId = UUID.randomUUID();
            var directionId = UUID.randomUUID();
            var planId = UUID.randomUUID();
            var dateFrom = OffsetDateTime.now().minusDays(7);
            var dateTo = OffsetDateTime.now();
            var pageable = PageRequest.of(0, 10);
            var sessionLog = SessionLog.builder()
                    .userId(userId)
                    .directionId(directionId)
                    .planId(planId)
                    .startedAt(dateFrom.plusDays(1))
                    .durationMinutes(30)
                    .build();

            when(directionRepository.existsByIdAndUserProfileId(directionId, userId))
                    .thenReturn(true);
            when(planRepository.existsByIdAndUserId(planId, userId))
                    .thenReturn(true);
            when(sessionLogRepository.findByFilters(userId, directionId, planId, dateFrom, dateTo, pageable))
                    .thenReturn(new PageImpl<>(List.of(sessionLog), pageable, 1));

            var result = service.findAll(userId, directionId, planId, dateFrom, dateTo, pageable);

            assertThat(result.getContent()).containsExactly(sessionLog);

            verify(sessionLogRepository).findByFilters(userId, directionId, planId, dateFrom, dateTo, pageable);
        }
    }

    @Nested
    class Create {

        @Test
        @DisplayName("deve criar sessao manual usando actualMinutes")
        void shouldCreateManualSessionUsingActualMinutes() {
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

            when(sessionLogRepository.save(any(SessionLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = service.create(userId, request);

            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getStartedAt()).isEqualTo(startedAt);
            assertThat(result.getDurationMinutes()).isEqualTo(30);
            assertThat(result.getResult()).isEqualTo("Resultado");
            assertThat(result.getNotes()).isEqualTo("Notas");

            verify(sessionLogRepository).save(any(SessionLog.class));
        }

        @Test
        @DisplayName("deve criar sessao manual calculando duracao por startedAt e finishedAt")
        void shouldCreateManualSessionCalculatingDuration() {
            var userId = UUID.randomUUID();
            var startedAt = OffsetDateTime.now();
            var finishedAt = startedAt.plusMinutes(45);

            var request = new CreateSessionLogRequest(
                    null,
                    null,
                    startedAt,
                    finishedAt,
                    null,
                    "Resultado",
                    "Notas"
            );

            when(sessionLogRepository.save(any(SessionLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = service.create(userId, request);

            assertThat(result.getDurationMinutes()).isEqualTo(45);
        }

        @Test
        @DisplayName("deve lançar erro quando não houver actualMinutes nem finishedAt")
        void shouldThrowWhenDurationCannotBeResolved() {
            var userId = UUID.randomUUID();

            var request = new CreateSessionLogRequest(
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null,
                    null,
                    null
            );

            assertThatThrownBy(() -> service.create(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("finishedAt é obrigatório quando actualMinutes não for informado.");

            verify(sessionLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve validar ownership do plano quando planId for informado")
        void shouldValidatePlanOwnership() {
            var userId = UUID.randomUUID();
            var planId = UUID.randomUUID();

            var request = new CreateSessionLogRequest(
                    planId,
                    null,
                    OffsetDateTime.now(),
                    null,
                    25,
                    null,
                    null
            );

            when(planRepository.existsByIdAndUserId(planId, userId))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.create(userId, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(sessionLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve validar ownership da direção quando directionId for informado")
        void shouldValidateDirectionOwnership() {
            var userId = UUID.randomUUID();
            var directionId = UUID.randomUUID();

            var request = new CreateSessionLogRequest(
                    null,
                    directionId,
                    OffsetDateTime.now(),
                    null,
                    25,
                    null,
                    null
            );

            when(directionRepository.existsByIdAndUserProfileId(directionId, userId))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.create(userId, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(sessionLogRepository, never()).save(any());
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("deve retornar sessão quando pertence ao usuário")
        void shouldReturnSessionWhenOwnedByUser() {
            var userId = UUID.randomUUID();
            var id = UUID.randomUUID();

            var sessionLog = SessionLog.builder()
                    .id(id)
                    .userId(userId)
                    .startedAt(OffsetDateTime.now())
                    .durationMinutes(30)
                    .build();

            when(sessionLogRepository.findByIdAndUserId(id, userId))
                    .thenReturn(Optional.of(sessionLog));

            var result = service.findById(userId, id);

            assertThat(result).isEqualTo(sessionLog);
        }

        @Test
        @DisplayName("deve lançar not found quando sessão não pertence ao usuário")
        void shouldThrowWhenSessionDoesNotBelongToUser() {
            var userId = UUID.randomUUID();
            var id = UUID.randomUUID();

            when(sessionLogRepository.findByIdAndUserId(id, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(userId, id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class AutomaticFromPlan {

        @Test
        @DisplayName("deve criar SessionLog automático a partir de plano")
        void shouldCreateAutomaticSessionFromPlan() {
            var userId = UUID.randomUUID();
            var planId = UUID.randomUUID();
            var directionId = UUID.randomUUID();
            var startedAt = OffsetDateTime.now();
            var finishedAt = startedAt.plusMinutes(50);

            when(planRepository.existsByIdAndUserId(planId, userId))
                    .thenReturn(true);
            when(directionRepository.existsByIdAndUserProfileId(directionId, userId))
                    .thenReturn(true);
            when(sessionLogRepository.save(any(SessionLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = service.createAutomaticFromPlan(
                    userId,
                    planId,
                    directionId,
                    startedAt,
                    finishedAt,
                    null,
                    "Plano concluído",
                    "Sessão automática"
            );

            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getPlanId()).isEqualTo(planId);
            assertThat(result.getDirectionId()).isEqualTo(directionId);
            assertThat(result.getDurationMinutes()).isEqualTo(50);
            assertThat(result.getResult()).isEqualTo("Plano concluído");
            assertThat(result.getNotes()).isEqualTo("Sessão automática");
        }
    }
}
