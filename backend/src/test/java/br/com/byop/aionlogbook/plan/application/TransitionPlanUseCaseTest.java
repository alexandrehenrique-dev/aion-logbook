package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.dto.*;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.InvalidPlanTransitionException;
import br.com.byop.aionlogbook.shared.error.PlanInProgressConflictException;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransitionPlanUseCaseTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanEventRepository planEventRepository;

    @Mock
    private PlanMapper planMapper;

    private TransitionPlanUseCase useCase;

    private UUID userId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();

        var clock = Clock.fixed(NOW, ZONE);

        useCase = new TransitionPlanUseCase(
                planRepository,
                planEventRepository,
                planMapper,
                clock
        );
    }

    @Nested
    class Start {

        @Test
        void shouldStartScheduledPlan() {
            var plan = plan(PlanStatus.SCHEDULED);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.existsByUserIdAndStatusAndIdNot(userId, PlanStatus.IN_PROGRESS, planId))
                    .thenReturn(false);
            when(planRepository.save(plan)).thenReturn(plan);

            useCase.start(userId, planId, new StartPlanRequest("Started"));

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.IN_PROGRESS);
            assertThat(plan.getStartedAt()).isEqualTo(NOW);
            assertThat(plan.getUpdatedAt()).isEqualTo(NOW);
            assertThat(plan.getLastStatusChangedAt()).isEqualTo(NOW);

            var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
            verify(planEventRepository).save(eventCaptor.capture());

            var event = eventCaptor.getValue();

            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getPlanId()).isEqualTo(planId);
            assertThat(event.getEventType()).isEqualTo(PlanEventType.STARTED);
            assertThat(event.getFromStatus()).isEqualTo(PlanStatus.SCHEDULED);
            assertThat(event.getToStatus()).isEqualTo(PlanStatus.IN_PROGRESS);
            assertThat(event.getDescription()).isEqualTo("Started");
            assertThat(event.getCreatedAt()).isEqualTo(NOW);
        }

        @Test
        void shouldThrowConflictWhenAnotherPlanIsInProgress() {
            var plan = plan(PlanStatus.SCHEDULED);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.existsByUserIdAndStatusAndIdNot(userId, PlanStatus.IN_PROGRESS, planId))
                    .thenReturn(true);

            assertThatThrownBy(() -> useCase.start(userId, planId, new StartPlanRequest("Started")))
                    .isInstanceOf(PlanInProgressConflictException.class);

            verify(planRepository, never()).save(any());
            verify(planEventRepository, never()).save(any());
        }

        @Test
        void shouldThrowInvalidTransitionWhenPlanIsCompleted() {
            var plan = plan(PlanStatus.COMPLETED);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));

            assertThatThrownBy(() -> useCase.start(userId, planId, new StartPlanRequest("Started")))
                    .isInstanceOf(InvalidPlanTransitionException.class);

            verify(planRepository, never()).existsByUserIdAndStatusAndIdNot(any(), any(), any());
            verify(planRepository, never()).save(any());
            verify(planEventRepository, never()).save(any());
        }
    }

    @Nested
    class Complete {

        @Test
        void shouldCompleteInProgressPlan() {
            var plan = plan(PlanStatus.IN_PROGRESS);
            plan.setStartedAt(NOW.minusSeconds(1800));

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.save(plan)).thenReturn(plan);

            useCase.complete(userId, planId, new CompletePlanRequest(null, "Completed"));

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.COMPLETED);
            assertThat(plan.getFinishedAt()).isEqualTo(NOW);
            assertThat(plan.getActualMinutes()).isEqualTo(30);
            assertThat(plan.getLastStatusChangedAt()).isEqualTo(NOW);

            var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
            verify(planEventRepository).save(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.COMPLETED);
            assertThat(eventCaptor.getValue().getFromStatus()).isEqualTo(PlanStatus.IN_PROGRESS);
            assertThat(eventCaptor.getValue().getToStatus()).isEqualTo(PlanStatus.COMPLETED);
        }

        @Test
        void shouldUseProvidedActualMinutesWhenCompleting() {
            var plan = plan(PlanStatus.IN_PROGRESS);
            plan.setStartedAt(NOW.minusSeconds(1800));

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.save(plan)).thenReturn(plan);

            useCase.complete(userId, planId, new CompletePlanRequest(10, "Completed"));

            assertThat(plan.getActualMinutes()).isEqualTo(10);
        }

        @Test
        void shouldThrowInvalidTransitionWhenCompletingPendingPlan() {
            var plan = plan(PlanStatus.PENDING);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));

            assertThatThrownBy(() -> useCase.complete(userId, planId, new CompletePlanRequest(null, "Completed")))
                    .isInstanceOf(InvalidPlanTransitionException.class);

            verify(planRepository, never()).save(any());
            verify(planEventRepository, never()).save(any());
        }
    }

    @Nested
    class Partial {

        @Test
        void shouldPartiallyCompleteInProgressPlan() {
            var plan = plan(PlanStatus.IN_PROGRESS);
            plan.setStartedAt(NOW.minusSeconds(900));

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.save(plan)).thenReturn(plan);

            useCase.partial(userId, planId, new PartialPlanRequest(null, "Tired", "Partial"));

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.PARTIAL);
            assertThat(plan.getReason()).isEqualTo("Tired");
            assertThat(plan.getFinishedAt()).isEqualTo(NOW);
            assertThat(plan.getActualMinutes()).isEqualTo(15);

            var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
            verify(planEventRepository).save(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.PARTIAL_COMPLETED);
            assertThat(eventCaptor.getValue().getFromStatus()).isEqualTo(PlanStatus.IN_PROGRESS);
            assertThat(eventCaptor.getValue().getToStatus()).isEqualTo(PlanStatus.PARTIAL);
        }
    }

    @Nested
    class Postpone {

        @Test
        void shouldPostponePlanAndRecalculatePlannedEndAt() {
            var plan = plan(PlanStatus.PENDING);
            var newStart = NOW.plusSeconds(3600);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.save(plan)).thenReturn(plan);

            useCase.postpone(userId, planId, new PostponePlanRequest(newStart, 45, "Need more time"));

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.POSTPONED);
            assertThat(plan.getPlannedStartAt()).isEqualTo(newStart);
            assertThat(plan.getEstimatedMinutes()).isEqualTo(45);
            assertThat(plan.getPlannedEndAt()).isEqualTo(newStart.plusSeconds(2700));
            assertThat(plan.getReason()).isEqualTo("Need more time");

            var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
            verify(planEventRepository).save(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.POSTPONED);
        }
    }

    @Nested
    class Ignore {

        @Test
        void shouldIgnorePlan() {
            var plan = plan(PlanStatus.DUE);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.save(plan)).thenReturn(plan);

            useCase.ignore(userId, planId, new IgnorePlanRequest("Not relevant anymore"));

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.IGNORED);
            assertThat(plan.getReason()).isEqualTo("Not relevant anymore");

            var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
            verify(planEventRepository).save(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.IGNORED);
        }
    }

    @Nested
    class Cancel {

        @Test
        void shouldCancelNonTerminalPlan() {
            var plan = plan(PlanStatus.IN_PROGRESS);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.save(plan)).thenReturn(plan);

            useCase.cancel(userId, planId, new CancelPlanRequest("Canceled by user"));

            assertThat(plan.getStatus()).isEqualTo(PlanStatus.CANCELED);
            assertThat(plan.getReason()).isEqualTo("Canceled by user");

            var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
            verify(planEventRepository).save(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.CANCELED);
        }
    }

    @Nested
    class Modify {

        @Test
        void shouldModifyNonTerminalPlan() {
            var plan = plan(PlanStatus.SCHEDULED);
            var newStart = NOW.plusSeconds(7200);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));
            when(planRepository.save(plan)).thenReturn(plan);

            var request = new ModifyPlanRequest(
                    "New title",
                    "New description",
                    null,
                    null,
                    null,
                    newStart,
                    60,
                    true,
                    newStart.minusSeconds(600),
                    "Changed plan"
            );

            useCase.modify(userId, planId, request);

            assertThat(plan.getTitle()).isEqualTo("New title");
            assertThat(plan.getDescription()).isEqualTo("New description");
            assertThat(plan.getPlannedStartAt()).isEqualTo(newStart);
            assertThat(plan.getEstimatedMinutes()).isEqualTo(60);
            assertThat(plan.getPlannedEndAt()).isEqualTo(newStart.plusSeconds(3600));
            assertThat(plan.isNotify()).isTrue();
            assertThat(plan.getNotificationDateTime()).isEqualTo(newStart.minusSeconds(600));
            assertThat(plan.getReason()).isEqualTo("Changed plan");
            assertThat(plan.getLastStatusChangedAt()).isEqualTo(NOW);

            var eventCaptor = ArgumentCaptor.forClass(PlanEvent.class);
            verify(planEventRepository).save(eventCaptor.capture());

            assertThat(eventCaptor.getValue().getEventType()).isEqualTo(PlanEventType.MODIFIED);
            assertThat(eventCaptor.getValue().getFromStatus()).isEqualTo(PlanStatus.SCHEDULED);
            assertThat(eventCaptor.getValue().getToStatus()).isEqualTo(PlanStatus.SCHEDULED);
        }

        @Test
        void shouldThrowInvalidTransitionWhenModifyingTerminalPlan() {
            var plan = plan(PlanStatus.CANCELED);

            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.of(plan));

            var request = new ModifyPlanRequest(
                    "New title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Changed plan"
            );

            assertThatThrownBy(() -> useCase.modify(userId, planId, request))
                    .isInstanceOf(InvalidPlanTransitionException.class);

            verify(planRepository, never()).save(any());
            verify(planEventRepository, never()).save(any());
        }
    }

    @Nested
    class Ownership {

        @Test
        void shouldThrowResourceNotFoundWhenPlanDoesNotBelongToUser() {
            when(planRepository.findByIdAndUserId(planId, userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.start(userId, planId, new StartPlanRequest("Started")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Plan not found");

            verify(planRepository, never()).save(any());
            verify(planEventRepository, never()).save(any());
        }
    }

    private Plan plan(PlanStatus status) {
        var plan = new Plan();

        plan.setId(planId);
        plan.setUserId(userId);
        plan.setTitle("Plan");
        plan.setStatus(status);
        plan.setCreatedAt(NOW.minusSeconds(3600));
        plan.setUpdatedAt(NOW.minusSeconds(3600));
        plan.setLastStatusChangedAt(NOW.minusSeconds(3600));

        return plan;
    }
}