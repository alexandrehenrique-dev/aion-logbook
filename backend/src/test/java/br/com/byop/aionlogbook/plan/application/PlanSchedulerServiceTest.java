package br.com.byop.aionlogbook.plan.application;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanSchedulerServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PlanEventRepository planEventRepository;

    private PlanSchedulerService service;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new PlanSchedulerService(planRepository, planEventRepository, clock);
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
