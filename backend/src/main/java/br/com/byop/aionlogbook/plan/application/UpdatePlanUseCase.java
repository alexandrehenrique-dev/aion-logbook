package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.notification.application.NotificationService;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.dto.PlanResponse;
import br.com.byop.aionlogbook.plan.dto.UpdatePlanRequest;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class UpdatePlanUseCase {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("America/Sao_Paulo");

    private final PlanRepository planRepository;
    private final PlanEventRepository planEventRepository;
    private final DirectionRepository directionRepository;
    private final PlanMapper planMapper;
    private final NotificationService notificationService;
    private final Clock clock;

    @Autowired
    public UpdatePlanUseCase(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            DirectionRepository directionRepository,
            PlanMapper planMapper,
            NotificationService notificationService
    ) {
        this(
                planRepository,
                planEventRepository,
                directionRepository,
                planMapper,
                notificationService,
                Clock.system(APPLICATION_ZONE)
        );
    }

    UpdatePlanUseCase(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            DirectionRepository directionRepository,
            PlanMapper planMapper,
            NotificationService notificationService,
            Clock clock
    ) {
        this.planRepository = planRepository;
        this.planEventRepository = planEventRepository;
        this.directionRepository = directionRepository;
        this.planMapper = planMapper;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional
    public PlanResponse execute(UUID userId, UUID planId, UpdatePlanRequest request) {
        var plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        validateDirectionOwnership(userId, request.directionId());

        var previousStatus = plan.getStatus();
        var originalPlannedStartAt = plan.getPlannedStartAt();
        var now = Instant.now(clock);

        planMapper.applyUpdate(plan, request, now);
        plan.setPlannedEndAt(calculatePlannedEndAt(plan));

        // Rescheduling: the plan already had a time and the new time is different
        boolean wasRescheduled = request.plannedStartAt() != null
                && originalPlannedStartAt != null
                && !Objects.equals(request.plannedStartAt(), originalPlannedStartAt);

        if (wasRescheduled) {
            notificationService.deleteReminderForPlan(plan.getId(), plan.getUserId());
        }

        var savedPlan = planRepository.save(plan);

        planEventRepository.save(wasRescheduled
                ? rescheduledEvent(savedPlan, previousStatus, now)
                : updatedEvent(savedPlan, previousStatus, now));

        return planMapper.toResponse(savedPlan);
    }

    private void validateDirectionOwnership(UUID userId, UUID directionId) {
        if (directionId == null) {
            return;
        }

        var exists = directionRepository.existsByIdAndUserProfileIdAndStatus(
                directionId,
                userId,
                DirectionStatus.ACTIVE
        );

        if (!exists) {
            throw new ResourceNotFoundException("Direction not found");
        }
    }

    private Instant calculatePlannedEndAt(Plan plan) {
        if (plan.getPlannedStartAt() == null || plan.getEstimatedMinutes() == null) {
            return null;
        }

        return plan.getPlannedStartAt().plusSeconds(plan.getEstimatedMinutes() * 60L);
    }

    private PlanEvent updatedEvent(Plan plan, PlanStatus previousStatus, Instant now) {
        var event = new PlanEvent();

        event.setId(UUID.randomUUID());
        event.setUserId(plan.getUserId());
        event.setPlanId(plan.getId());
        event.setEventType(PlanEventType.UPDATED);
        event.setFromStatus(previousStatus);
        event.setToStatus(plan.getStatus());
        event.setDescription("Plan updated");
        event.setMetadata(Map.of("source", "UpdatePlanUseCase"));
        event.setCreatedAt(now);

        return event;
    }

    private PlanEvent rescheduledEvent(Plan plan, PlanStatus previousStatus, Instant now) {
        var event = new PlanEvent();

        event.setId(UUID.randomUUID());
        event.setUserId(plan.getUserId());
        event.setPlanId(plan.getId());
        event.setEventType(PlanEventType.RESCHEDULED);
        event.setFromStatus(previousStatus);
        event.setToStatus(plan.getStatus());
        event.setDescription("Plan rescheduled");
        event.setMetadata(Map.of(
                "source", "UpdatePlanUseCase",
                "newPlannedStartAt", String.valueOf(plan.getPlannedStartAt())
        ));
        event.setCreatedAt(now);

        return event;
    }
}
