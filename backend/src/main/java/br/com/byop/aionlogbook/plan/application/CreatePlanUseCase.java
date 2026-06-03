package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.dto.CreatePlanRequest;
import br.com.byop.aionlogbook.plan.dto.PlanResponse;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Component
public class CreatePlanUseCase {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("America/Sao_Paulo");

    private final PlanRepository planRepository;
    private final PlanEventRepository planEventRepository;
    private final DirectionRepository directionRepository;
    private final PlanMapper planMapper;
    private final Clock clock;

    @Autowired
    public CreatePlanUseCase(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            DirectionRepository directionRepository,
            PlanMapper planMapper
    ) {
        this(
                planRepository,
                planEventRepository,
                directionRepository,
                planMapper,
                Clock.system(APPLICATION_ZONE)
        );
    }

    CreatePlanUseCase(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            DirectionRepository directionRepository,
            PlanMapper planMapper,
            Clock clock
    ) {
        this.planRepository = planRepository;
        this.planEventRepository = planEventRepository;
        this.directionRepository = directionRepository;
        this.planMapper = planMapper;
        this.clock = clock;
    }

    @Transactional
    public PlanResponse execute(UUID userId, CreatePlanRequest request) {
        validateDirectionOwnership(userId, request.directionId());

        var now = Instant.now(clock);
        var plan = planMapper.toEntity(request, userId, now);

        plan.setStatus(resolveInitialStatus(plan, now));
        plan.setPlannedEndAt(calculatePlannedEndAt(plan));
        plan.setLastStatusChangedAt(now);

        var savedPlan = planRepository.save(plan);

        planEventRepository.save(createdEvent(savedPlan, now));

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

    private PlanStatus resolveInitialStatus(Plan plan, Instant now) {
        if (plan.getPlannedDate() == null && plan.getPlannedStartAt() == null) {
            return PlanStatus.DRAFT;
        }

        if (plan.getPlannedStartAt() != null && plan.getPlannedStartAt().isAfter(now)) {
            return PlanStatus.SCHEDULED;
        }

        if (plan.getPlannedDate() != null && plan.getPlannedStartAt() == null) {
            var today = LocalDate.now(clock);

            if (plan.getPlannedDate().isEqual(today)) {
                return PlanStatus.PENDING;
            }
        }

        if (plan.getPlannedStartAt() != null && !plan.getPlannedStartAt().isAfter(now)) {
            return PlanStatus.DUE;
        }

        return PlanStatus.SCHEDULED;
    }

    private Instant calculatePlannedEndAt(Plan plan) {
        if (plan.getPlannedStartAt() == null || plan.getEstimatedMinutes() == null) {
            return null;
        }

        return plan.getPlannedStartAt().plusSeconds(plan.getEstimatedMinutes() * 60L);
    }

    private PlanEvent createdEvent(Plan plan, Instant now) {
        var event = new PlanEvent();

        event.setId(UUID.randomUUID());
        event.setUserId(plan.getUserId());
        event.setPlanId(plan.getId());
        event.setEventType(PlanEventType.CREATED);
        event.setFromStatus(null);
        event.setToStatus(plan.getStatus());
        event.setDescription("Plan created");
        event.setMetadata(Map.of("source", "CreatePlanUseCase"));
        event.setCreatedAt(now);

        return event;
    }
}
