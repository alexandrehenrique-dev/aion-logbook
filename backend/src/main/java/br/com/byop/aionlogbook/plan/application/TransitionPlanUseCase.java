package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.PlanTransitionPolicy;
import br.com.byop.aionlogbook.plan.dto.*;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.InvalidPlanTransitionException;
import br.com.byop.aionlogbook.shared.error.PlanInProgressConflictException;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Component
public class TransitionPlanUseCase {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("America/Sao_Paulo");

    private final PlanRepository planRepository;
    private final PlanEventRepository planEventRepository;
    private final PlanMapper planMapper;
    private final Clock clock;

    @Autowired
    public TransitionPlanUseCase(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            PlanMapper planMapper
    ) {
        this(
                planRepository,
                planEventRepository,
                planMapper,
                Clock.system(APPLICATION_ZONE)
        );
    }

    TransitionPlanUseCase(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            PlanMapper planMapper,
            Clock clock
    ) {
        this.planRepository = planRepository;
        this.planEventRepository = planEventRepository;
        this.planMapper = planMapper;
        this.clock = clock;
    }

    @Transactional
    public PlanResponse start(UUID userId, UUID planId, StartPlanRequest request) {
        var plan = findOwned(userId, planId);

        validate(PlanTransitionPolicy.canStart(plan.getStatus()));

        var hasAnotherInProgress = planRepository.existsByUserIdAndStatusAndIdNot(
                userId,
                PlanStatus.IN_PROGRESS,
                plan.getId()
        );

        if (hasAnotherInProgress) {
            throw new PlanInProgressConflictException();
        }

        var savedPlan = transition(
                plan,
                PlanStatus.IN_PROGRESS,
                PlanEventType.STARTED,
                request.description(),
                now -> plan.setStartedAt(now)
        );

        return planMapper.toResponse(savedPlan);
    }

    @Transactional
    public PlanResponse complete(UUID userId, UUID planId, CompletePlanRequest request) {
        var plan = findOwned(userId, planId);

        validate(PlanTransitionPolicy.canComplete(plan.getStatus()));

        var savedPlan = transition(
                plan,
                PlanStatus.COMPLETED,
                PlanEventType.COMPLETED,
                request.description(),
                now -> {
                    plan.setFinishedAt(now);
                    plan.setActualMinutes(resolveActualMinutes(plan, request.actualMinutes(), now));
                }
        );

        return planMapper.toResponse(savedPlan);
    }

    @Transactional
    public PlanResponse partial(UUID userId, UUID planId, PartialPlanRequest request) {
        var plan = findOwned(userId, planId);

        validate(PlanTransitionPolicy.canPartial(plan.getStatus()));

        var savedPlan = transition(
                plan,
                PlanStatus.PARTIAL,
                PlanEventType.PARTIAL_COMPLETED,
                request.description(),
                now -> {
                    plan.setReason(request.reason());
                    plan.setFinishedAt(now);
                    plan.setActualMinutes(resolveActualMinutes(plan, request.actualMinutes(), now));
                }
        );

        return planMapper.toResponse(savedPlan);
    }

    @Transactional
    public PlanResponse postpone(UUID userId, UUID planId, PostponePlanRequest request) {
        var plan = findOwned(userId, planId);

        validate(PlanTransitionPolicy.canPostpone(plan.getStatus()));

        var savedPlan = transition(
                plan,
                PlanStatus.POSTPONED,
                PlanEventType.POSTPONED,
                request.reason(),
                now -> {
                    plan.setPlannedStartAt(request.plannedStartAt());
                    plan.setEstimatedMinutes(request.estimatedMinutes());

                    if (request.plannedStartAt() != null && request.estimatedMinutes() != null) {
                        plan.setPlannedEndAt(request.plannedStartAt().plus(Duration.ofMinutes(request.estimatedMinutes())));
                    } else {
                        plan.setPlannedEndAt(null);
                    }

                    plan.setReason(request.reason());
                }
        );

        return planMapper.toResponse(savedPlan);
    }

    @Transactional
    public PlanResponse ignore(UUID userId, UUID planId, IgnorePlanRequest request) {
        var plan = findOwned(userId, planId);

        validate(PlanTransitionPolicy.canIgnore(plan.getStatus()));

        var savedPlan = transition(
                plan,
                PlanStatus.IGNORED,
                PlanEventType.IGNORED,
                request.reason(),
                now -> plan.setReason(request.reason())
        );

        return planMapper.toResponse(savedPlan);
    }

    @Transactional
    public PlanResponse cancel(UUID userId, UUID planId, CancelPlanRequest request) {
        var plan = findOwned(userId, planId);

        validate(PlanTransitionPolicy.canCancel(plan.getStatus()));

        var savedPlan = transition(
                plan,
                PlanStatus.CANCELED,
                PlanEventType.CANCELED,
                request.reason(),
                now -> plan.setReason(request.reason())
        );

        return planMapper.toResponse(savedPlan);
    }

    @Transactional
    public PlanResponse modify(UUID userId, UUID planId, ModifyPlanRequest request) {
        var plan = findOwned(userId, planId);

        validate(PlanTransitionPolicy.canModify(plan.getStatus()));

        var previousStatus = plan.getStatus();
        var now = Instant.now(clock);

        if (request.title() != null) {
            plan.setTitle(request.title());
        }

        if (request.description() != null) {
            plan.setDescription(request.description());
        }

        if (request.type() != null) {
            plan.setType(request.type());
        }

        if (request.priority() != null) {
            plan.setPriority(request.priority());
        }

        if (request.plannedDate() != null) {
            plan.setPlannedDate(request.plannedDate());
        }

        if (request.plannedStartAt() != null) {
            plan.setPlannedStartAt(request.plannedStartAt());
        }

        if (request.estimatedMinutes() != null) {
            plan.setEstimatedMinutes(request.estimatedMinutes());
        }

        if (request.notificationEnabled() != null) {
            plan.setNotify(request.notificationEnabled());
        }

        if (request.notificationDateTime() != null) {
            plan.setNotificationDateTime(request.notificationDateTime());
        }

        if (request.reason() != null) {
            plan.setReason(request.reason());
        }

        plan.setPlannedEndAt(calculatePlannedEndAt(plan));
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        var savedPlan = planRepository.save(plan);

        planEventRepository.save(event(
                savedPlan,
                PlanEventType.MODIFIED,
                previousStatus,
                savedPlan.getStatus(),
                request.reason(),
                now
        ));

        return planMapper.toResponse(savedPlan);
    }

    private Plan findOwned(UUID userId, UUID planId) {
        return planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
    }

    private Plan transition(
            Plan plan,
            PlanStatus toStatus,
            PlanEventType eventType,
            String description,
            TransitionMutation mutation
    ) {
        var previousStatus = plan.getStatus();
        var now = Instant.now(clock);

        mutation.apply(now);

        plan.setStatus(toStatus);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        var savedPlan = planRepository.save(plan);

        planEventRepository.save(event(
                savedPlan,
                eventType,
                previousStatus,
                toStatus,
                description,
                now
        ));

        return savedPlan;
    }

    private PlanEvent event(
            Plan plan,
            PlanEventType eventType,
            PlanStatus fromStatus,
            PlanStatus toStatus,
            String description,
            Instant now
    ) {
        var event = new PlanEvent();

        event.setId(UUID.randomUUID());
        event.setUserId(plan.getUserId());
        event.setPlanId(plan.getId());
        event.setEventType(eventType);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setDescription(description);
        event.setMetadata(Map.of("source", "TransitionPlanUseCase"));
        event.setCreatedAt(now);

        return event;
    }

    private Instant calculatePlannedEndAt(Plan plan) {
        if (plan.getPlannedStartAt() == null || plan.getEstimatedMinutes() == null) {
            return null;
        }

        return plan.getPlannedStartAt().plus(Duration.ofMinutes(plan.getEstimatedMinutes()));
    }

    private Integer resolveActualMinutes(Plan plan, Integer provided, Instant now) {
        if (provided != null) {
            return provided;
        }

        if (plan.getStartedAt() == null) {
            return null;
        }

        return Math.toIntExact(Duration.between(plan.getStartedAt(), now).toMinutes());
    }

    private void validate(boolean allowed) {
        if (!allowed) {
            throw new InvalidPlanTransitionException();
        }
    }

    @FunctionalInterface
    private interface TransitionMutation {
        void apply(Instant now);
    }
}