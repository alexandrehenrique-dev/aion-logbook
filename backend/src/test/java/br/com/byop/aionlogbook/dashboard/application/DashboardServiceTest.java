package br.com.byop.aionlogbook.dashboard.application;

import br.com.byop.aionlogbook.dashboard.dto.DashboardSessionProjection;
import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.session.infrastructure.SessionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final Instant FIXED_NOW = Instant.parse("2026-06-04T13:30:00Z");

    @Mock
    private PlanRepository planRepository;

    @Mock
    private DirectionRepository directionRepository;

    @Mock
    private SessionLogRepository sessionLogRepository;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(
                planRepository,
                directionRepository,
                sessionLogRepository,
                Clock.fixed(FIXED_NOW, ZONE_ID)
        );
    }

    @Nested
    class Today {

        @Test
        void shouldReturnDashboardTodayAggregatedByUser() {
            UUID userId = UUID.randomUUID();
            UUID directionId = UUID.randomUUID();
            UUID planId = UUID.randomUUID();

            Plan pendingPlan = mockPlan(planId, directionId, "Estudar backend", PlanStatus.PENDING);
            Plan completedPlan = mockPlan(UUID.randomUUID(), directionId, "Finalizar API", PlanStatus.COMPLETED);
            DashboardSessionProjection session = mockSessionProjection(planId, directionId);

            when(planRepository.findByUserIdAndStatuses(userId, List.of(PlanStatus.IN_PROGRESS)))
                    .thenReturn(List.of());

            when(planRepository.findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.DUE))
                    .thenReturn(List.of());

            when(planRepository.findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.MISSED))
                    .thenReturn(List.of());

            when(planRepository.findTodayByUserIdAndStatuses(
                    userId,
                    LocalDate.of(2026, 6, 4),
                    List.of(PlanStatus.COMPLETED, PlanStatus.PARTIAL)
            )).thenReturn(List.of(completedPlan));

            when(planRepository.findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.PENDING))
                    .thenReturn(List.of(pendingPlan));

            when(sessionLogRepository.sumDurationMinutesByUserIdBetween(
                    userId,
                    LocalDate.of(2026, 6, 4).atStartOfDay(ZONE_ID).toOffsetDateTime(),
                    LocalDate.of(2026, 6, 5).atStartOfDay(ZONE_ID).toOffsetDateTime()
            )).thenReturn(90);

            when(directionRepository.countByUserProfileIdAndStatus(userId, DirectionStatus.ACTIVE))
                    .thenReturn(3L);

            when(sessionLogRepository.findLastDashboardSessions(userId, PageRequest.of(0, 5)))
                    .thenReturn(List.of(session));

            var response = service.today(userId);

            assertThat(response.date()).isEqualTo(LocalDate.of(2026, 6, 4));
            assertThat(response.greeting()).isEqualTo("Bom dia");
            assertThat(response.plansPending()).hasSize(1);
            assertThat(response.plansCompleted()).hasSize(1);
            assertThat(response.totalEnergyMinutes()).isEqualTo(90);
            assertThat(response.activeDirections()).isEqualTo(3L);
            assertThat(response.completionRate()).isEqualTo(50.0);
            assertThat(response.lastSessions()).hasSize(1);
            assertThat(response.lastSessions().getFirst().planTitle()).isEqualTo("Estudar backend");
            assertThat(response.lastSessions().getFirst().directionName()).isEqualTo("Projeto Aion");
            assertThat(response.lastLogEntries()).isEmpty();

            verify(planRepository).findByUserIdAndStatuses(userId, List.of(PlanStatus.IN_PROGRESS));
            verify(planRepository).findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.DUE);
            verify(planRepository).findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.MISSED);
            verify(planRepository).findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.PENDING);
            verify(directionRepository).countByUserProfileIdAndStatus(userId, DirectionStatus.ACTIVE);
            verifyNoMoreInteractions(directionRepository);
        }

        @Test
        void shouldReturnZeroCompletionRateWhenThereAreNoPlans() {
            UUID userId = UUID.randomUUID();

            when(planRepository.findByUserIdAndStatuses(userId, List.of(PlanStatus.IN_PROGRESS)))
                    .thenReturn(List.of());

            when(planRepository.findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.DUE))
                    .thenReturn(List.of());

            when(planRepository.findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.MISSED))
                    .thenReturn(List.of());

            when(planRepository.findTodayByUserIdAndStatuses(
                    userId,
                    LocalDate.of(2026, 6, 4),
                    List.of(PlanStatus.COMPLETED, PlanStatus.PARTIAL)
            )).thenReturn(List.of());

            when(planRepository.findTodayByUserIdAndStatus(userId, LocalDate.of(2026, 6, 4), PlanStatus.PENDING))
                    .thenReturn(List.of());

            when(sessionLogRepository.sumDurationMinutesByUserIdBetween(any(), any(), any()))
                    .thenReturn(null);

            when(directionRepository.countByUserProfileIdAndStatus(userId, DirectionStatus.ACTIVE))
                    .thenReturn(0L);

            when(sessionLogRepository.findLastDashboardSessions(userId, PageRequest.of(0, 5)))
                    .thenReturn(List.of());

            var response = service.today(userId);

            assertThat(response.completionRate()).isZero();
            assertThat(response.totalEnergyMinutes()).isZero();
            assertThat(response.activeDirections()).isZero();
            assertThat(response.lastSessions()).isEmpty();
            assertThat(response.lastLogEntries()).isEmpty();
        }
    }

    @Nested
    class Summary {

        @Test
        void shouldReturnDashboardSummaryAggregatedByUser() {
            UUID userId = UUID.randomUUID();
            OffsetDateTime lastActivity = OffsetDateTime.parse("2026-06-04T09:00:00-03:00");

            when(planRepository.countByUserId(userId)).thenReturn(10L);
            when(planRepository.countByUserIdAndStatusIn(userId, List.of(PlanStatus.COMPLETED, PlanStatus.PARTIAL)))
                    .thenReturn(7L);

            when(sessionLogRepository.sumTotalDurationMinutesByUserId(userId))
                    .thenReturn(600);

            when(sessionLogRepository.sumDurationMinutesByUserIdBetween(
                    userId,
                    LocalDate.of(2026, 6, 1).atStartOfDay(ZONE_ID).toOffsetDateTime(),
                    LocalDate.of(2026, 6, 5).atStartOfDay(ZONE_ID).toOffsetDateTime()
            )).thenReturn(180);

            when(directionRepository.countByUserProfileIdAndStatus(userId, DirectionStatus.ACTIVE))
                    .thenReturn(2L);

            when(sessionLogRepository.findLastActivity(userId))
                    .thenReturn(lastActivity);

            var response = service.summary(userId);

            assertThat(response.totalTimeMinutes()).isEqualTo(600);
            assertThat(response.weeklyTimeMinutes()).isEqualTo(180);
            assertThat(response.plansCreated()).isEqualTo(10L);
            assertThat(response.plansCompleted()).isEqualTo(7L);
            assertThat(response.completionRate()).isEqualTo(70.0);
            assertThat(response.activeDirections()).isEqualTo(2L);
            assertThat(response.streak()).isZero();
            assertThat(response.lastActivity()).isEqualTo(lastActivity);
        }

        @Test
        void shouldReturnZeroCompletionRateWhenNoPlansWereCreated() {
            UUID userId = UUID.randomUUID();

            when(planRepository.countByUserId(userId)).thenReturn(0L);
            when(planRepository.countByUserIdAndStatusIn(userId, List.of(PlanStatus.COMPLETED, PlanStatus.PARTIAL)))
                    .thenReturn(0L);
            when(sessionLogRepository.sumTotalDurationMinutesByUserId(userId)).thenReturn(null);
            when(sessionLogRepository.sumDurationMinutesByUserIdBetween(any(), any(), any())).thenReturn(null);
            when(directionRepository.countByUserProfileIdAndStatus(userId, DirectionStatus.ACTIVE)).thenReturn(0L);
            when(sessionLogRepository.findLastActivity(userId)).thenReturn(null);

            var response = service.summary(userId);

            assertThat(response.completionRate()).isZero();
            assertThat(response.totalTimeMinutes()).isZero();
            assertThat(response.weeklyTimeMinutes()).isZero();
            assertThat(response.plansCreated()).isZero();
            assertThat(response.plansCompleted()).isZero();
            assertThat(response.activeDirections()).isZero();
            assertThat(response.lastActivity()).isNull();
        }
    }

    private Plan mockPlan(UUID id, UUID directionId, String title, PlanStatus status) {
        Plan plan = mock(Plan.class);

        when(plan.getId()).thenReturn(id);
        when(plan.getDirectionId()).thenReturn(directionId);
        when(plan.getTitle()).thenReturn(title);
        when(plan.getStatus()).thenReturn(status);
        when(plan.getPlannedDate()).thenReturn(LocalDate.of(2026, 6, 4));
        when(plan.getPlannedStartAt()).thenReturn(Instant.parse("2026-06-04T10:00:00Z"));
        when(plan.getStartedAt()).thenReturn(Instant.parse("2026-06-04T10:05:00Z"));
        when(plan.getFinishedAt()).thenReturn(Instant.parse("2026-06-04T11:00:00Z"));
        when(plan.getEstimatedMinutes()).thenReturn(60);
        when(plan.getActualMinutes()).thenReturn(55);

        return plan;
    }

    private DashboardSessionProjection mockSessionProjection(UUID planId, UUID directionId) {
        DashboardSessionProjection session = mock(DashboardSessionProjection.class);

        when(session.getId()).thenReturn(UUID.randomUUID());
        when(session.getPlanId()).thenReturn(planId);
        when(session.getDirectionId()).thenReturn(directionId);
        when(session.getPlanTitle()).thenReturn("Estudar backend");
        when(session.getDirectionName()).thenReturn("Projeto Aion");
        when(session.getDurationMinutes()).thenReturn(55);
        when(session.getStartedAt()).thenReturn(OffsetDateTime.parse("2026-06-04T07:05:00-03:00"));
        when(session.getFinishedAt()).thenReturn(OffsetDateTime.parse("2026-06-04T08:00:00-03:00"));
        when(session.getCreatedAt()).thenReturn(OffsetDateTime.parse("2026-06-04T07:00:00-03:00"));

        return session;
    }
}
