package br.com.byop.aionlogbook.analytics.api;

import br.com.byop.aionlogbook.analytics.application.AnalyticsService;
import br.com.byop.aionlogbook.analytics.dto.AnalyticsOverviewResponse;
import br.com.byop.aionlogbook.analytics.dto.PlannedVsExecutedResponse;
import br.com.byop.aionlogbook.analytics.dto.PlansByDayResponse;
import br.com.byop.aionlogbook.analytics.dto.StatusDistributionResponse;
import br.com.byop.aionlogbook.analytics.dto.TimeByDirectionResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserService currentUserService;

    public AnalyticsController(
            AnalyticsService analyticsService,
            CurrentUserService currentUserService
    ) {
        this.analyticsService = analyticsService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/overview")
    public AnalyticsOverviewResponse overview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo
    ) {
        var userId = currentUserService.currentUserId();

        return analyticsService.getOverview(userId, dateFrom, dateTo);
    }

    @GetMapping("/plans-by-day")
    public List<PlansByDayResponse> plansByDay(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo
    ) {
        var userId = currentUserService.currentUserId();

        return analyticsService.getPlansByDay(userId, dateFrom, dateTo);
    }

    @GetMapping("/status-distribution")
    public List<StatusDistributionResponse> statusDistribution(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo
    ) {
        var userId = currentUserService.currentUserId();

        return analyticsService.getStatusDistribution(userId, dateFrom, dateTo);
    }

    @GetMapping("/time-by-direction")
    public List<TimeByDirectionResponse> timeByDirection(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo
    ) {
        var userId = currentUserService.currentUserId();

        return analyticsService.getTimeByDirection(userId, dateFrom, dateTo);
    }

    @GetMapping("/planned-vs-executed")
    public List<PlannedVsExecutedResponse> plannedVsExecuted(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo
    ) {
        var userId = currentUserService.currentUserId();

        return analyticsService.getPlannedVsExecuted(userId, dateFrom, dateTo);
    }
}
