package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlanSchedulerService {

    private static final int BATCH_SIZE = 500;

    private final PlanRepository planRepository;
    private final PlanEventRepository planEventRepository;
    private final Clock clock;

    public PlanSchedulerService(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            Clock clock
    ) {
        this.planRepository = planRepository;
        this.planEventRepository = planEventRepository;
        this.clock = clock;
    }

    @Transactional
    public int markDuePlans() {
        var now = Instant.now(clock);

        var plans = planRepository.findDueCandidates(
                List.of(PlanStatus.SCHEDULED, PlanStatus.PENDING),
                now,
                PageRequest.of(0, BATCH_SIZE)
        );

        plans.forEach(plan -> transition(plan, PlanStatus.DUE, PlanEventType.DUE, now));

        return plans.size();
    }

    @Transactional
    public int markMissedPlans() {
        var now = Instant.now(clock);

        var plans = planRepository.findMissedCandidates(
                PlanStatus.DUE,
                now,
                PageRequest.of(0, BATCH_SIZE)
        );

        plans.forEach(plan -> transition(plan, PlanStatus.MISSED, PlanEventType.MISSED, now));

        return plans.size();
    }

    private void transition(
            Plan plan,
            PlanStatus toStatus,
            PlanEventType eventType,
            Instant now
    ) {
        var fromStatus = plan.getStatus();

        if (fromStatus == toStatus) {
            return;
        }

        plan.setStatus(toStatus);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        planRepository.save(plan);

        createEventOnce(plan, eventType, fromStatus, toStatus, now);
    }

    private void createEventOnce(
            Plan plan,
            PlanEventType eventType,
            PlanStatus fromStatus,
            PlanStatus toStatus,
            Instant now
    ) {
        if (planEventRepository.existsByPlanIdAndEventType(plan.getId(), eventType)) {
            return;
        }

        var event = new PlanEvent();

        event.setId(UUID.randomUUID());
        event.setUserId(plan.getUserId());
        event.setPlanId(plan.getId());
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setDescription(null);
        event.setMetadata(Map.of("source", "PlanSchedulerService"));
        event.setCreatedAt(now);

        try {
            planEventRepository.save(event);
        } catch (DataIntegrityViolationException ignored) {
            // Idempotencia contra corrida entre execucoes/instancias sem ShedLock.
        }
    }
}
