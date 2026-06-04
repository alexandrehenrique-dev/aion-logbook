package br.com.byop.aionlogbook.dashboard.api;

import br.com.byop.aionlogbook.dashboard.application.DashboardService;
import br.com.byop.aionlogbook.dashboard.dto.DashboardSummaryResponse;
import br.com.byop.aionlogbook.dashboard.dto.DashboardTodayResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public DashboardController(
            DashboardService dashboardService,
            CurrentUserService currentUserService
    ) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/today")
    public DashboardTodayResponse today() {
        var userId = currentUserService.currentUserId();

        return dashboardService.today(userId);
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        var userId = currentUserService.currentUserId();

        return dashboardService.summary(userId);
    }
}