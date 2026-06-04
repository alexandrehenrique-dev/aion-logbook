package br.com.byop.aionlogbook.analytics.application;

import br.com.byop.aionlogbook.analytics.dto.AnalyticsOverviewResponse;
import br.com.byop.aionlogbook.analytics.dto.PlannedVsExecutedResponse;
import br.com.byop.aionlogbook.analytics.dto.PlansByDayResponse;
import br.com.byop.aionlogbook.analytics.dto.StatusDistributionResponse;
import br.com.byop.aionlogbook.analytics.dto.TimeByDirectionResponse;
import br.com.byop.aionlogbook.analytics.infrastructure.AnalyticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AnalyticsService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public AnalyticsOverviewResponse getOverview(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var overview = repository.overview(userId, dateFrom, dateTo);

        var completionRate = percentage(
                overview.plansCompleted() + overview.plansPartial(),
                overview.plansCreated()
        );

        return new AnalyticsOverviewResponse(
                overview.plansCreated(),
                overview.plansCompleted(),
                overview.plansPartial(),
                overview.plansMissed(),
                overview.plansIgnored(),
                overview.plansCanceled(),
                overview.activeDirections(),
                overview.totalTimeMinutes(),
                overview.weeklyTimeMinutes(),
                completionRate
        );
    }

    public List<PlansByDayResponse> getPlansByDay(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        return repository.plansByDay(userId, dateFrom, dateTo)
                .stream()
                .map(item -> new PlansByDayResponse(
                        dayName(item.date()),
                        item.date(),
                        item.planned(),
                        item.executed()
                ))
                .toList();
    }

    public List<StatusDistributionResponse> getStatusDistribution(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var items = repository.statusDistribution(userId, dateFrom, dateTo);

        var total = items.stream()
                .mapToLong(item -> item.count())
                .sum();

        return items.stream()
                .map(item -> new StatusDistributionResponse(
                        item.status(),
                        item.count(),
                        percentage(item.count(), total)
                ))
                .toList();
    }

    public List<TimeByDirectionResponse> getTimeByDirection(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        var items = repository.timeByDirection(userId, dateFrom, dateTo);

        var totalMinutes = items.stream()
                .mapToLong(item -> item.totalMinutes())
                .sum();

        return items.stream()
                .map(item -> new TimeByDirectionResponse(
                        item.directionId(),
                        item.directionName(),
                        item.color(),
                        item.totalMinutes(),
                        percentage(item.totalMinutes(), totalMinutes),
                        item.sessionsCount()
                ))
                .toList();
    }

    public List<PlannedVsExecutedResponse> getPlannedVsExecuted(UUID userId, LocalDate dateFrom, LocalDate dateTo) {
        return repository.plannedVsExecuted(userId, dateFrom, dateTo)
                .stream()
                .map(item -> new PlannedVsExecutedResponse(
                        dayName(item.date()),
                        item.date(),
                        item.plannedMinutes(),
                        item.executedMinutes()
                ))
                .toList();
    }

    private double percentage(long value, long total) {
        if (total <= 0) {
            return 0.0;
        }

        return (value * 100.0) / total;
    }

    private String dayName(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, PT_BR);
    }
}
