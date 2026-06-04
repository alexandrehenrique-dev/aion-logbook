package br.com.byop.aionlogbook.dashboard.application;

import br.com.byop.aionlogbook.dashboard.dto.DashboardLogEntryResponse;
import br.com.byop.aionlogbook.dashboard.dto.DashboardPlanResponse;
import br.com.byop.aionlogbook.dashboard.dto.DashboardSessionProjection;
import br.com.byop.aionlogbook.dashboard.dto.DashboardSessionResponse;
import br.com.byop.aionlogbook.dashboard.dto.DashboardSummaryResponse;
import br.com.byop.aionlogbook.dashboard.dto.DashboardTodayResponse;
import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.session.infrastructure.SessionLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final int LAST_SESSIONS_LIMIT = 5;

    private final PlanRepository planRepository;
    private final DirectionRepository directionRepository;
    private final SessionLogRepository sessionLogRepository;
    private final Clock clock;

    public DashboardService(
            PlanRepository planRepository,
            DirectionRepository directionRepository,
            SessionLogRepository sessionLogRepository,
            Clock clock
    ) {
        this.planRepository = planRepository;
        this.directionRepository = directionRepository;
        this.sessionLogRepository = sessionLogRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardTodayResponse today(UUID userId) {
        LocalDate today = LocalDate.now(clock.withZone(DEFAULT_ZONE_ID));
        LocalTime localTime = LocalTime.now(clock.withZone(DEFAULT_ZONE_ID));

        var startOfDay = today.atStartOfDay(DEFAULT_ZONE_ID).toOffsetDateTime();
        var endOfDay = today.plusDays(1).atStartOfDay(DEFAULT_ZONE_ID).toOffsetDateTime();

        var plansInProgress = planRepository.findByUserIdAndStatuses(
                userId,
                List.of(PlanStatus.IN_PROGRESS)
        );

        var plansDue = planRepository.findTodayByUserIdAndStatus(
                userId,
                today,
                PlanStatus.DUE
        );

        var plansMissed = planRepository.findTodayByUserIdAndStatus(
                userId,
                today,
                PlanStatus.MISSED
        );

        var plansCompleted = planRepository.findTodayByUserIdAndStatuses(
                userId,
                today,
                List.of(PlanStatus.COMPLETED, PlanStatus.PARTIAL)
        );

        var plansPending = planRepository.findTodayByUserIdAndStatus(
                userId,
                today,
                PlanStatus.PENDING
        );

        Integer totalEnergyMinutes = sessionLogRepository.sumDurationMinutesByUserIdBetween(
                userId,
                startOfDay,
                endOfDay
        );

        Long activeDirections = directionRepository.countByUserProfileIdAndStatus(
                userId,
                DirectionStatus.ACTIVE
        );

        var lastSessions = sessionLogRepository.findLastDashboardSessions(
                        userId,
                        PageRequest.of(0, LAST_SESSIONS_LIMIT)
                )
                .stream()
                .map(this::toSessionResponse)
                .toList();

        Double completionRate = calculateCompletionRate(
                plansPending.size(),
                plansDue.size(),
                plansMissed.size(),
                plansCompleted.size()
        );

        return new DashboardTodayResponse(
                today,
                greeting(localTime),
                plansInProgress.stream().map(this::toPlanResponse).toList(),
                plansDue.stream().map(this::toPlanResponse).toList(),
                plansMissed.stream().map(this::toPlanResponse).toList(),
                plansCompleted.stream().map(this::toPlanResponse).toList(),
                plansPending.stream().map(this::toPlanResponse).toList(),
                safeInteger(totalEnergyMinutes),
                completionRate,
                activeDirections,
                lastSessions,
                List.<DashboardLogEntryResponse>of()
        );
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(UUID userId) {
        LocalDate today = LocalDate.now(clock.withZone(DEFAULT_ZONE_ID));

        var startOfWeek = today.with(DayOfWeek.MONDAY)
                .atStartOfDay(DEFAULT_ZONE_ID)
                .toOffsetDateTime();

        var endOfTomorrow = today.plusDays(1)
                .atStartOfDay(DEFAULT_ZONE_ID)
                .toOffsetDateTime();

        Long plansCreated = planRepository.countByUserId(userId);

        Long plansCompleted = planRepository.countByUserIdAndStatusIn(
                userId,
                List.of(PlanStatus.COMPLETED, PlanStatus.PARTIAL)
        );

        Integer totalTimeMinutes = sessionLogRepository.sumTotalDurationMinutesByUserId(userId);

        Integer weeklyTimeMinutes = sessionLogRepository.sumDurationMinutesByUserIdBetween(
                userId,
                startOfWeek,
                endOfTomorrow
        );

        Long activeDirections = directionRepository.countByUserProfileIdAndStatus(
                userId,
                DirectionStatus.ACTIVE
        );

        return new DashboardSummaryResponse(
                safeInteger(totalTimeMinutes),
                calculateSummaryCompletionRate(plansCreated, plansCompleted),
                plansCreated,
                plansCompleted,
                activeDirections,
                safeInteger(weeklyTimeMinutes),
                0,
                sessionLogRepository.findLastActivity(userId)
        );
    }

    private DashboardPlanResponse toPlanResponse(Plan plan) {
        return new DashboardPlanResponse(
                plan.getId(),
                plan.getDirectionId(),
                plan.getTitle(),
                plan.getStatus(),
                plan.getPlannedDate(),
                plan.getPlannedStartAt(),
                plan.getStartedAt(),
                plan.getFinishedAt(),
                plan.getEstimatedMinutes(),
                plan.getActualMinutes()
        );
    }

    private DashboardSessionResponse toSessionResponse(DashboardSessionProjection session) {
        return new DashboardSessionResponse(
                session.getId(),
                session.getPlanId(),
                session.getDirectionId(),
                session.getPlanTitle(),
                session.getDirectionName(),
                safeInteger(session.getDurationMinutes()),
                session.getStartedAt(),
                session.getFinishedAt(),
                session.getCreatedAt()
        );
    }

    private String greeting(LocalTime time) {
        if (time.isBefore(LocalTime.NOON)) {
            return "Bom dia";
        }

        if (time.isBefore(LocalTime.of(18, 0))) {
            return "Boa tarde";
        }

        return "Boa noite";
    }

    private Double calculateCompletionRate(
            int pending,
            int due,
            int missed,
            int completed
    ) {
        int total = pending + due + missed + completed;

        if (total == 0) {
            return 0.0;
        }

        return completed * 100.0 / total;
    }

    private Double calculateSummaryCompletionRate(Long created, Long completed) {
        if (created == null || created == 0) {
            return 0.0;
        }

        return safeLong(completed) * 100.0 / created;
    }

    private Integer safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
