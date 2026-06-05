package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.notification.application.NotificationService;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.dto.UpdatePlanRequest;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UpdatePlanUseCaseTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");

    private final PlanRepository planRepository = mock(PlanRepository.class);
    private final PlanEventRepository planEventRepository = mock(PlanEventRepository.class);
    private final DirectionRepository directionRepository = mock(DirectionRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final PlanMapper planMapper = new PlanMapper();

    private final UpdatePlanUseCase useCase = new UpdatePlanUseCase(
            planRepository,
            planEventRepository,
            directionRepository,
            planMapper,
            notificationService,
            Clock.fixed(NOW, ZONE)
    );

    @Test
    void shouldUpdatePlanAndCreateUpdatedEvent() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var plan = plan(userId, planId);
        var plannedStartAt = Instant.parse("2026-06-04T10:00:00Z");

        var request = new UpdatePlanRequest(
                directionId,
                "Plano atualizado",
                "Descrição atualizada",
                "WORK",
                Priority.CRITICAL,
                LocalDate.parse("2026-06-04"),
                plannedStartAt,
                90,
                true,
                Instant.parse("2026-06-04T09:45:00Z"),
                "Motivo atualizado",
                List.of("deep-work")
        );

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
        when(directionRepository.existsByIdAndUserProfileIdAndStatus(
                directionId,
                userId,
                DirectionStatus.ACTIVE
        )).thenReturn(true);
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, planId, request);

        var planCaptor = ArgumentCaptor.forClass(Plan.class);
        var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);

        verify(planRepository).save(planCaptor.capture());
        verify(planEventRepository).save(eventCaptor.capture());

        var savedPlan = planCaptor.getValue();
        var savedEvent = eventCaptor.getValue();

        assertThat(response.id()).isEqualTo(planId);
        assertThat(response.title()).isEqualTo("Plano atualizado");
        assertThat(response.description()).isEqualTo("Descrição atualizada");
        assertThat(response.type()).isEqualTo("WORK");
        assertThat(response.priority()).isEqualTo(Priority.CRITICAL);
        assertThat(response.plannedDate()).isEqualTo(LocalDate.parse("2026-06-04"));
        assertThat(response.plannedStartAt()).isEqualTo(plannedStartAt);
        assertThat(response.plannedEndAt()).isEqualTo(plannedStartAt.plusSeconds(90 * 60L));
        assertThat(response.estimatedMinutes()).isEqualTo(90);
        assertThat(response.notificationEnabled()).isTrue();
        assertThat(response.reason()).isEqualTo("Motivo atualizado");
        assertThat(response.tags()).containsExactly("deep-work");

        assertThat(savedPlan.getUpdatedAt()).isEqualTo(NOW);
        assertThat(savedPlan.getPlannedEndAt()).isEqualTo(plannedStartAt.plusSeconds(90 * 60L));

        assertThat(savedEvent.getEventType()).isEqualTo(PlanEventType.UPDATED);
        assertThat(savedEvent.getPlanId()).isEqualTo(planId);
        assertThat(savedEvent.getUserId()).isEqualTo(userId);
        assertThat(savedEvent.getFromStatus()).isEqualTo(PlanStatus.DRAFT);
        assertThat(savedEvent.getToStatus()).isEqualTo(PlanStatus.DRAFT);
        assertThat(savedEvent.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldPreserveUnprovidedFields() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = plan(userId, planId);

        var request = new UpdatePlanRequest(
                null,
                "Somente título novo",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, planId, request);

        assertThat(response.title()).isEqualTo("Somente título novo");
        assertThat(response.description()).isEqualTo("Descrição original");
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.estimatedMinutes()).isEqualTo(30);
        assertThat(response.tags()).containsExactly("original");

        verify(directionRepository, never()).existsByIdAndUserProfileIdAndStatus(any(), any(), any());
        verify(planEventRepository).save(any(PlanEvent.class));
    }

    @Test
    void shouldRecalculatePlannedEndAtWhenStartAndEstimatedMinutesAreProvided() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = plan(userId, planId);
        var plannedStartAt = Instant.parse("2026-06-10T08:00:00Z");

        var request = new UpdatePlanRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                plannedStartAt,
                120,
                null,
                null,
                null,
                null
        );

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, planId, request);

        assertThat(response.plannedEndAt()).isEqualTo(plannedStartAt.plusSeconds(120 * 60L));
    }

    @Test
    void shouldSetPlannedEndAtToNullWhenThereIsNoPlannedStartAt() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = plan(userId, planId);
        plan.setPlannedStartAt(null);

        var request = new UpdatePlanRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                120,
                null,
                null,
                null,
                null
        );

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, planId, request);

        assertThat(response.plannedEndAt()).isNull();
    }

    @Test
    void shouldThrowWhenPlanDoesNotBelongToUserOrDoesNotExist() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var request = emptyRequest();

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId, planId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plan not found");

        verify(planRepository, never()).save(any());
        verify(planEventRepository, never()).save(any());
    }

    @Test
    void shouldDeleteReminderAndEmitRescheduledEventWhenPlannedStartAtChanges() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = plan(userId, planId);
        var originalStart = Instant.parse("2026-06-04T15:30:00Z");
        plan.setPlannedStartAt(originalStart);

        var newStart = Instant.parse("2026-06-04T17:30:00Z");
        var request = new UpdatePlanRequest(
                null, null, null, null, null, null,
                newStart,
                null, null, null, null, null
        );

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, planId, request);

        verify(notificationService).deleteReminderForPlan(planId, userId);

        var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
        verify(planEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.RESCHEDULED);
    }

    @Test
    void shouldNotDeleteReminderWhenPlannedStartAtDoesNotChange() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = plan(userId, planId);

        var request = new UpdatePlanRequest(
                null, "Novo título", null, null, null, null,
                null,
                null, null, null, null, null
        );

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(userId, planId, request);

        verify(notificationService, never()).deleteReminderForPlan(any(), any());

        var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
        verify(planEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.UPDATED);
    }

    @Test
    void shouldThrowWhenDirectionDoesNotBelongToUserOrIsNotActive() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var plan = plan(userId, planId);

        var request = new UpdatePlanRequest(
                directionId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
        when(directionRepository.existsByIdAndUserProfileIdAndStatus(
                directionId,
                userId,
                DirectionStatus.ACTIVE
        )).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(userId, planId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Direction not found");

        verify(planRepository, never()).save(any());
        verify(planEventRepository, never()).save(any());
    }

    private static UpdatePlanRequest emptyRequest() {
        return new UpdatePlanRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static Plan plan(UUID userId, UUID planId) {
        var now = Instant.parse("2026-06-01T12:00:00Z");

        var plan = new Plan();
        plan.setId(planId);
        plan.setUserId(userId);
        plan.setDirectionId(null);
        plan.setTitle("Plano original");
        plan.setDescription("Descrição original");
        plan.setType("STUDY");
        plan.setPriority(Priority.MEDIUM);
        plan.setStatus(PlanStatus.DRAFT);
        plan.setPlannedDate(LocalDate.parse("2026-06-03"));
        plan.setPlannedStartAt(null);
        plan.setPlannedEndAt(null);
        plan.setEstimatedMinutes(30);
        plan.setNotify(false);
        plan.setNotificationDateTime(null);
        plan.setReason("Motivo original");
        plan.setTags(List.of("original"));
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        return plan;
    }
}
