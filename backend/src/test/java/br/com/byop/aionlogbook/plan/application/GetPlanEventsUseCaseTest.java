package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GetPlanEventsUseCaseTest {

    private final PlanRepository planRepository = mock(PlanRepository.class);
    private final PlanEventRepository planEventRepository = mock(PlanEventRepository.class);
    private final PlanMapper planMapper = new PlanMapper();

    private final GetPlanEventsUseCase useCase = new GetPlanEventsUseCase(
            planRepository,
            planEventRepository,
            planMapper
    );

    @Test
    void shouldListPlanEventsInRepositoryOrderWhenPlanBelongsToUser() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();

        var plan = plan(userId, planId);
        var created = event(
                userId,
                planId,
                PlanEventType.CREATED,
                null,
                PlanStatus.DRAFT,
                Instant.parse("2026-06-03T10:00:00Z")
        );
        var updated = event(
                userId,
                planId,
                PlanEventType.UPDATED,
                PlanStatus.DRAFT,
                PlanStatus.DRAFT,
                Instant.parse("2026-06-03T11:00:00Z")
        );

        when(planRepository.findByIdAndUserId(planId, userId))
                .thenReturn(Optional.of(plan));

        when(planEventRepository.findByPlanIdAndUserIdOrderByCreatedAtAsc(planId, userId))
                .thenReturn(List.of(created, updated));

        var result = useCase.execute(userId, planId);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).eventType()).isEqualTo(PlanEventType.CREATED);
        assertThat(result.get(0).fromStatus()).isNull();
        assertThat(result.get(0).toStatus()).isEqualTo(PlanStatus.DRAFT);
        assertThat(result.get(0).createdAt()).isEqualTo(Instant.parse("2026-06-03T10:00:00Z"));

        assertThat(result.get(1).eventType()).isEqualTo(PlanEventType.UPDATED);
        assertThat(result.get(1).fromStatus()).isEqualTo(PlanStatus.DRAFT);
        assertThat(result.get(1).toStatus()).isEqualTo(PlanStatus.DRAFT);
        assertThat(result.get(1).createdAt()).isEqualTo(Instant.parse("2026-06-03T11:00:00Z"));

        verify(planRepository).findByIdAndUserId(planId, userId);
        verify(planEventRepository).findByPlanIdAndUserIdOrderByCreatedAtAsc(planId, userId);
    }

    @Test
    void shouldReturnEmptyListWhenPlanHasNoEvents() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = plan(userId, planId);

        when(planRepository.findByIdAndUserId(planId, userId))
                .thenReturn(Optional.of(plan));

        when(planEventRepository.findByPlanIdAndUserIdOrderByCreatedAtAsc(planId, userId))
                .thenReturn(List.of());

        var result = useCase.execute(userId, planId);

        assertThat(result).isEmpty();

        verify(planRepository).findByIdAndUserId(planId, userId);
        verify(planEventRepository).findByPlanIdAndUserIdOrderByCreatedAtAsc(planId, userId);
    }

    @Test
    void shouldThrowWhenPlanDoesNotExistOrDoesNotBelongToUser() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();

        when(planRepository.findByIdAndUserId(planId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId, planId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plan not found");

        verify(planRepository).findByIdAndUserId(planId, userId);
        verify(planEventRepository, never()).findByPlanIdAndUserIdOrderByCreatedAtAsc(any(), any());
    }

    private static Plan plan(UUID userId, UUID planId) {
        var now = Instant.parse("2026-06-03T12:00:00Z");

        var plan = new Plan();
        plan.setId(planId);
        plan.setUserId(userId);
        plan.setDirectionId(UUID.randomUUID());
        plan.setTitle("Plano de teste");
        plan.setDescription("Descrição de teste");
        plan.setType("STUDY");
        plan.setPriority(Priority.MEDIUM);
        plan.setStatus(PlanStatus.DRAFT);
        plan.setPlannedDate(LocalDate.parse("2026-06-03"));
        plan.setPlannedStartAt(null);
        plan.setPlannedEndAt(null);
        plan.setEstimatedMinutes(60);
        plan.setNotify(false);
        plan.setNotificationDateTime(null);
        plan.setStartedAt(null);
        plan.setFinishedAt(null);
        plan.setActualMinutes(null);
        plan.setReason("Motivo teste");
        plan.setTags(List.of("java", "spring"));
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        return plan;
    }

    private static PlanEvent event(
            UUID userId,
            UUID planId,
            PlanEventType eventType,
            PlanStatus fromStatus,
            PlanStatus toStatus,
            Instant createdAt
    ) {
        var event = new PlanEvent();

        event.setId(UUID.randomUUID());
        event.setUserId(userId);
        event.setPlanId(planId);
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setDescription("Evento de teste");
        event.setMetadata(Map.of("source", "test"));
        event.setCreatedAt(createdAt);

        return event;
    }
}
