package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.dto.PlanResponse;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class GetPlanUseCase {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    public GetPlanUseCase(
            PlanRepository planRepository,
            PlanMapper planMapper
    ) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    @Transactional(readOnly = true)
    public PlanResponse execute(UUID userId, UUID planId) {
        var plan = planRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        return planMapper.toResponse(plan);
    }
}
