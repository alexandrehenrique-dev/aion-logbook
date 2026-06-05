package br.com.byop.aionlogbook.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardTodayResponse(
        LocalDate date,
        String greeting,
        List<DashboardPlanResponse> plansInProgress,
        List<DashboardPlanResponse> plansDue,
        List<DashboardPlanResponse> plansMissed,
        List<DashboardPlanResponse> plansCompleted,
        List<DashboardPlanResponse> plansPending,
        List<DashboardPlanResponse> plansScheduled,
        Integer totalEnergyMinutes,
        Double completionRate,
        Long activeDirections,
        List<DashboardSessionResponse> lastSessions,
        List<DashboardLogEntryResponse> lastLogEntries
) {
}