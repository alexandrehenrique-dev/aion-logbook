package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.dto.PlanEventResponse;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class GetPlanEventsUseCase {

    private final PlanRepository planRepository;
    private final PlanEventRepository planEventRepository;
    private final PlanMapper planMapper;

    public GetPlanEventsUseCase(
            PlanRepository planRepository,
            PlanEventRepository planEventRepository,
            PlanMapper planMapper
    ) {
        this.planRepository = planRepository;
        this.planEventRepository = planEventRepository;
        this.planMapper = planMapper;
    }

    @Transactional(readOnly = true)
    public List<PlanEventResponse> execute(UUID userId, UUID planId) {
        var exists = planRepository.findByIdAndUserId(planId, userId).isPresent();

        if (!exists) {
            throw new ResourceNotFoundException("Plan not found");
        }

        return planEventRepository.findByPlanIdAndUserIdOrderByCreatedAtAsc(planId, userId)
                .stream()
                .map(planMapper::toEventResponse)
                .toList();
    }
}
