package br.com.byop.aionlogbook.direction.application;

import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.dto.DirectionSummaryResponse;
import br.com.byop.aionlogbook.direction.mapper.DirectionMapper;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.session.infrastructure.SessionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
public class DirectionSummaryService {

    private static final Set<PlanStatus> COMPLETED_STATUSES = EnumSet.of(
            PlanStatus.COMPLETED,
            PlanStatus.PARTIAL
    );

    private static final Set<PlanStatus> ACTIVE_STATUSES = EnumSet.of(
            PlanStatus.SCHEDULED,
            PlanStatus.PENDING,
            PlanStatus.DUE,
            PlanStatus.IN_PROGRESS
    );

    private final DirectionService directionService;
    private final PlanRepository planRepository;
    private final SessionLogRepository sessionLogRepository;

    public DirectionSummaryService(
            DirectionService directionService,
            PlanRepository planRepository,
            SessionLogRepository sessionLogRepository
    ) {
        this.directionService = directionService;
        this.planRepository = planRepository;
        this.sessionLogRepository = sessionLogRepository;
    }

    @Transactional(readOnly = true)
    public DirectionSummaryResponse getSummary(UUID directionId) {
        Direction direction = directionService.findById(directionId);
        UUID userId = direction.getUserProfile().getId();

        long totalPlans = planRepository.countByUserIdAndDirectionId(userId, directionId);
        long completedPlans = planRepository.countByUserIdAndDirectionIdAndStatusIn(
                userId, directionId, COMPLETED_STATUSES);
        long activePlans = planRepository.countByUserIdAndDirectionIdAndStatusIn(
                userId, directionId, ACTIVE_STATUSES);
        long totalSessions = sessionLogRepository.countByUserIdAndDirectionId(userId, directionId);
        Integer rawMinutes = sessionLogRepository.sumDurationMinutesByUserIdAndDirectionId(userId, directionId);
        long totalSessionMinutes = rawMinutes != null ? rawMinutes : 0L;

        return DirectionMapper.toSummaryResponse(
                direction,
                totalPlans,
                completedPlans,
                activePlans,
                totalSessions,
                totalSessionMinutes
        );
    }
}
