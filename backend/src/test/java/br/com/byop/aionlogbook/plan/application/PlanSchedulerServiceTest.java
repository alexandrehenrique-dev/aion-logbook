package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.notification.application.NotificationService;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanSchedulerServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanEventRepository planEventRepository;

    @Mock
    private NotificationService notificationService;

    private PlanSchedulerService service;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new PlanSchedulerService(planRepository, planEventRepository, notificationService, clock);
    }

    @Test
    void shouldMarkScheduledAndPendingPlansAsDue() {
        var scheduled = plan(PlanStatus.SCHEDULED);
        var pending = plan(PlanStatus.PENDING);

        when(planRepository.findDueCandidates(
                anyCollection(),
                eq(NOW),
                any(Pageable.class)
        )).thenReturn(List.of(scheduled, pending));

        var count = service.markDuePlans();

        assertThat(count).isEqualTo(2);
        assertThat(scheduled.getStatus()).isEqualTo(PlanStatus.DUE);
        assertThat(pending.getStatus()).isEqualTo(PlanStatus.DUE);

        verify(planEventRepository, times(2)).save(any(PlanEvent.class));
    }

    @Test
    void shouldMarkDueExpiredPlansAsMissed() {
        var due = plan(PlanStatus.DUE);

        when(planRepository.findMissedCandidates(
                eq(PlanStatus.DUE),
                eq(NOW),
                any(Pageable.class)
        )).thenReturn(List.of(due));

        var count = service.markMissedPlans();

        assertThat(count).isEqualTo(1);
        assertThat(due.getStatus()).isEqualTo(PlanStatus.MISSED);

        verify(planEventRepository).save(any(PlanEvent.class));
    }

    @Test
    void shouldNotDuplicateDueEvent() {
        var plan = plan(PlanStatus.SCHEDULED);

        when(planRepository.findDueCandidates(
                anyCollection(),
                eq(NOW),
                any(Pageable.class)
        )).thenReturn(List.of(plan));

        when(planEventRepository.existsByPlanIdAndEventType(plan.getId(), PlanEventType.DUE))
                .thenReturn(true);

        service.markDuePlans();

        verify(planEventRepository, never()).save(any());
    }

    @Test
    void shouldSendReminderForEligiblePlan() {
        var plan = plan(PlanStatus.SCHEDULED);
        plan.setNotify(true);
        plan.setPlannedStartAt(NOW.plusSeconds(300)); // 5 min in the future — dentro da janela de 10 min

        when(planRepository.findReminderCandidates(
                eq(NOW),
                any(Instant.class),
                anyCollection(),
                any(Pageable.class)
        )).thenReturn(List.of(plan));

        when(notificationService.reminderAlreadySent(plan.getId(), plan.getUserId()))
                .thenReturn(false);

        var count = service.sendReminders();

        assertThat(count).isEqualTo(1);
        verify(notificationService).createPlanReminder(eq(plan), eq(NOW));
    }

    @Test
    void shouldNotSendDuplicateReminder() {
        var plan = plan(PlanStatus.SCHEDULED);
        plan.setNotify(true);

        when(planRepository.findReminderCandidates(
                eq(NOW),
                any(Instant.class),
                anyCollection(),
                any(Pageable.class)
        )).thenReturn(List.of(plan));

        when(notificationService.reminderAlreadySent(plan.getId(), plan.getUserId()))
                .thenReturn(true);

        var count = service.sendReminders();

        assertThat(count).isZero();
        verify(notificationService, never()).createPlanReminder(any(), any());
    }

    @Test
    void shouldReturnZeroWhenNoCandidates() {
        when(planRepository.findReminderCandidates(
                any(), any(), anyCollection(), any(Pageable.class)
        )).thenReturn(List.of());

        var count = service.sendReminders();

        assertThat(count).isZero();
        verify(notificationService, never()).createPlanReminder(any(), any());
    }

    private Plan plan(PlanStatus status) {
        var plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setUserId(UUID.randomUUID());
        plan.setTitle("Plano");
        plan.setStatus(status);
        plan.setPlannedStartAt(NOW.minusSeconds(60));
        plan.setPlannedEndAt(NOW.minusSeconds(1));
        plan.setCreatedAt(NOW.minusSeconds(3600));
        plan.setUpdatedAt(NOW.minusSeconds(3600));
        plan.setLastStatusChangedAt(NOW.minusSeconds(3600));
        return plan;
    }
}
