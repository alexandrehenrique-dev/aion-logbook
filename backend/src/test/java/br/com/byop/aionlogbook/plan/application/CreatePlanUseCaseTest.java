package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.dto.CreatePlanRequest;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CreatePlanUseCaseTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");

    private final PlanRepository planRepository = mock(PlanRepository.class);
    private final PlanEventRepository planEventRepository = mock(PlanEventRepository.class);
    private final DirectionRepository directionRepository = mock(DirectionRepository.class);
    private final PlanMapper planMapper = new PlanMapper();

    private final CreatePlanUseCase useCase = new CreatePlanUseCase(
            planRepository,
            planEventRepository,
            directionRepository,
            planMapper,
            Clock.fixed(NOW, ZONE)
    );

    @Test
    void shouldCreateDraftPlanWhenThereIsNoPlannedDateAndNoPlannedStartAt() {
        var userId = UUID.randomUUID();
        var request = request(null, null, null, null);

        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, request);

        var planCaptor = ArgumentCaptor.forClass(Plan.class);
        var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);

        verify(planRepository).save(planCaptor.capture());
        verify(planEventRepository).save(eventCaptor.capture());

        var savedPlan = planCaptor.getValue();
        var savedEvent = eventCaptor.getValue();

        assertThat(response.status()).isEqualTo(PlanStatus.DRAFT);
        assertThat(savedPlan.getUserId()).isEqualTo(userId);
        assertThat(savedPlan.getStatus()).isEqualTo(PlanStatus.DRAFT);
        assertThat(savedPlan.getPlannedEndAt()).isNull();

        assertThat(savedEvent.getEventType()).isEqualTo(PlanEventType.CREATED);
        assertThat(savedEvent.getPlanId()).isEqualTo(savedPlan.getId());
        assertThat(savedEvent.getUserId()).isEqualTo(userId);
        assertThat(savedEvent.getFromStatus()).isNull();
        assertThat(savedEvent.getToStatus()).isEqualTo(PlanStatus.DRAFT);
    }

    @Test
    void shouldCreateScheduledPlanWhenPlannedStartAtIsInTheFuture() {
        var userId = UUID.randomUUID();
        var plannedStartAt = NOW.plusSeconds(3600);
        var request = request(null, LocalDate.parse("2026-06-03"), plannedStartAt, 90);

        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, request);

        assertThat(response.status()).isEqualTo(PlanStatus.SCHEDULED);
        assertThat(response.plannedEndAt()).isEqualTo(plannedStartAt.plusSeconds(90 * 60L));
    }

    @Test
    void shouldCreatePendingPlanWhenPlannedDateIsTodayAndThereIsNoHour() {
        var userId = UUID.randomUUID();
        var today = LocalDate.now(Clock.fixed(NOW, ZONE));
        var request = request(null, today, null, null);

        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, request);

        assertThat(response.status()).isEqualTo(PlanStatus.PENDING);
    }

    @Test
    void shouldCreateDuePlanWhenPlannedStartAtIsNowOrPast() {
        var userId = UUID.randomUUID();
        var request = request(null, LocalDate.parse("2026-06-03"), NOW.minusSeconds(60), 30);

        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, request);

        assertThat(response.status()).isEqualTo(PlanStatus.DUE);
    }

    @Test
    void shouldValidateDirectionWhenDirectionIdIsProvided() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var request = request(directionId, null, null, null);

        when(directionRepository.existsByIdAndUserProfileIdAndStatus(
                directionId,
                userId,
                DirectionStatus.ACTIVE
        )).thenReturn(true);

        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, request);

        verify(directionRepository).existsByIdAndUserProfileIdAndStatus(
                directionId,
                userId,
                DirectionStatus.ACTIVE
        );
        verify(planRepository).save(any(Plan.class));
        verify(planEventRepository).save(any(PlanEvent.class));
    }

    @Test
    void shouldThrowWhenDirectionDoesNotBelongToUserOrIsNotActive() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var request = request(directionId, null, null, null);

        when(directionRepository.existsByIdAndUserProfileIdAndStatus(
                directionId,
                userId,
                DirectionStatus.ACTIVE
        )).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Direction not found");

        verify(planRepository, never()).save(any());
        verify(planEventRepository, never()).save(any());
    }

    private static CreatePlanRequest request(
            UUID directionId,
            LocalDate plannedDate,
            Instant plannedStartAt,
            Integer estimatedMinutes
    ) {
        return new CreatePlanRequest(
                directionId,
                "Plano de teste",
                "Descrição de teste",
                "STUDY",
                Priority.HIGH,
                plannedDate,
                plannedStartAt,
                estimatedMinutes,
                true,
                null,
                "Motivo teste",
                List.of("java", "spring")
        );
    }
}
