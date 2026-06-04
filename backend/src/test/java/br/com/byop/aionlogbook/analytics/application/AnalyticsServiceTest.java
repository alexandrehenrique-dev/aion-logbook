package br.com.byop.aionlogbook.analytics.application;

import br.com.byop.aionlogbook.analytics.domain.AnalyticsOverviewProjection;
import br.com.byop.aionlogbook.analytics.domain.PlannedVsExecutedProjection;
import br.com.byop.aionlogbook.analytics.domain.PlansByDayProjection;
import br.com.byop.aionlogbook.analytics.domain.StatusDistributionProjection;
import br.com.byop.aionlogbook.analytics.domain.TimeByDirectionProjection;
import br.com.byop.aionlogbook.analytics.infrastructure.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository repository;

    @InjectMocks
    private AnalyticsService service;

    @Test
    void shouldCalculateOverviewCompletionRate() {
        var userId = UUID.randomUUID();

        when(repository.overview(userId, null, null))
                .thenReturn(new AnalyticsOverviewProjection(
                        10,
                        4,
                        2,
                        1,
                        1,
                        2,
                        3,
                        180,
                        90
                ));

        var result = service.getOverview(userId, null, null);

        assertThat(result.totalPlans()).isEqualTo(10);
        assertThat(result.completedPlans()).isEqualTo(4);
        assertThat(result.partialPlans()).isEqualTo(2);
        assertThat(result.completionRate()).isEqualTo(60.0);
        assertThat(result.executedMinutes()).isEqualTo(180);
    }

    @Test
    void shouldReturnZeroCompletionRateWhenNoPlans() {
        var userId = UUID.randomUUID();

        when(repository.overview(userId, null, null))
                .thenReturn(new AnalyticsOverviewProjection(
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0
                ));

        var result = service.getOverview(userId, null, null);

        assertThat(result.totalPlans()).isZero();
        assertThat(result.completionRate()).isZero();
        assertThat(result.executedMinutes()).isZero();
    }

    @Test
    void shouldMapPlansByDay() {
        var userId = UUID.randomUUID();
        var date = LocalDate.of(2026, 6, 1);

        when(repository.plansByDay(userId, null, null))
                .thenReturn(List.of(
                        new PlansByDayProjection(date, 5, 3)
                ));

        var result = service.getPlansByDay(userId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().date()).isEqualTo(date);
        assertThat(result.getFirst().planned()).isEqualTo(5);
        assertThat(result.getFirst().executed()).isEqualTo(3);
        assertThat(result.getFirst().day()).isNotBlank();
    }

    @Test
    void shouldCalculateStatusDistributionPercentages() {
        var userId = UUID.randomUUID();

        when(repository.statusDistribution(userId, null, null))
                .thenReturn(List.of(
                        new StatusDistributionProjection("COMPLETED", 3),
                        new StatusDistributionProjection("PARTIAL", 1)
                ));

        var result = service.getStatusDistribution(userId, null, null);

        assertThat(result).hasSize(2);

        assertThat(result.getFirst().status()).isEqualTo("COMPLETED");
        assertThat(result.getFirst().total()).isEqualTo(3);
        assertThat(result.getFirst().percentage()).isEqualTo(75.0);

        assertThat(result.get(1).status()).isEqualTo("PARTIAL");
        assertThat(result.get(1).total()).isEqualTo(1);
        assertThat(result.get(1).percentage()).isEqualTo(25.0);
    }

    @Test
    void shouldReturnEmptyStatusDistributionWhenNoItems() {
        var userId = UUID.randomUUID();

        when(repository.statusDistribution(userId, null, null))
                .thenReturn(List.of());

        var result = service.getStatusDistribution(userId, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCalculateTimeByDirectionPercentages() {
        var userId = UUID.randomUUID();

        var directionA = UUID.randomUUID();
        var directionB = UUID.randomUUID();

        when(repository.timeByDirection(userId, null, null))
                .thenReturn(List.of(
                        new TimeByDirectionProjection(directionA, "Código", "#22c55e", 60, 2),
                        new TimeByDirectionProjection(directionB, "Estudo", "#6366f1", 40, 1)
                ));

        var result = service.getTimeByDirection(userId, null, null);

        assertThat(result).hasSize(2);

        assertThat(result.getFirst().directionId()).isEqualTo(directionA);
        assertThat(result.getFirst().totalMinutes()).isEqualTo(60);
        assertThat(result.getFirst().percentage()).isEqualTo(60.0);
        assertThat(result.getFirst().sessionsCount()).isEqualTo(2);

        assertThat(result.get(1).directionId()).isEqualTo(directionB);
        assertThat(result.get(1).totalMinutes()).isEqualTo(40);
        assertThat(result.get(1).percentage()).isEqualTo(40.0);
    }

    @Test
    void shouldReturnZeroPercentageWhenTimeByDirectionHasNoMinutes() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();

        when(repository.timeByDirection(userId, null, null))
                .thenReturn(List.of(
                        new TimeByDirectionProjection(directionId, "Código", "#22c55e", 0, 0)
                ));

        var result = service.getTimeByDirection(userId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().percentage()).isZero();
    }

    @Test
    void shouldMapPlannedVsExecuted() {
        var userId = UUID.randomUUID();
        var date = LocalDate.of(2026, 6, 1);

        when(repository.plannedVsExecuted(userId, null, null))
                .thenReturn(List.of(
                        new PlannedVsExecutedProjection(date, 120, 90)
                ));

        var result = service.getPlannedVsExecuted(userId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().date()).isEqualTo(date);
        assertThat(result.getFirst().plannedMinutes()).isEqualTo(120);
        assertThat(result.getFirst().executedMinutes()).isEqualTo(90);
        assertThat(result.getFirst().day()).isNotBlank();
    }
}
