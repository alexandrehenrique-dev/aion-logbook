package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.notification.application.NotificationService;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlanSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(PlanSchedulerService.class);
    private static final int BATCH_SIZE = 500;
    private static final Duration REMINDER_ADVANCE = Duration.ofMinutes(10);

    private final PlanRepository planRepository;
    private final PlanEventRepository planEventRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public PlanSchedulerService(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            NotificationService notificationService,
            Clock clock
    ) {
        this.planRepository = planRepository;
        this.planEventRepository = planEventRepository;
        this.notificationService = notificationService;
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

    @Transactional
    public int sendReminders() {
        var now = Instant.now(clock);
        // Finds plans whose reminder time has already passed:
        //   - explicit notificationDateTime <= now, OR
        //   - plannedStartAt <= now + 10 min (implicit 10-min-before window)
        var reminderCutoff = now.plus(REMINDER_ADVANCE);

        var candidates = planRepository.findReminderCandidates(
                now,
                reminderCutoff,
                List.of(PlanStatus.SCHEDULED, PlanStatus.PENDING),
                PageRequest.of(0, BATCH_SIZE)
        );

        log.info("scheduler.reminder.candidates count={}", candidates.size());

        int sent = 0;
        for (var plan : candidates) {
            if (notificationService.reminderAlreadySent(plan.getId(), plan.getUserId())) {
                log.debug("scheduler.reminder.skip planId={} — already sent", plan.getId());
                continue;
            }
            notificationService.createPlanReminder(plan, now);
            log.info("scheduler.reminder.sent planId={} title={}", plan.getId(), plan.getTitle());
            sent++;
        }
        return sent;
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
        } catch (DataIntegrityViolationException e) {
            log.debug("scheduler.event.duplicate planId={} eventType={} — race condition idempotent skip",
                    plan.getId(), eventType, e);
        }
    }
}
