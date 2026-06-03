package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.dto.PlanResponse;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class ListPlansUseCase {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    public ListPlansUseCase(
            PlanRepository planRepository,
            PlanMapper planMapper
    ) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    @Transactional(readOnly = true)
    public Page<PlanResponse> execute(
            UUID userId,
            PlanStatus status,
            UUID directionId,
            LocalDate plannedDate,
            Pageable pageable
    ) {
        return planRepository.findByFilters(userId, status, directionId, plannedDate, pageable)
                .map(planMapper::toResponse);
    }
}
