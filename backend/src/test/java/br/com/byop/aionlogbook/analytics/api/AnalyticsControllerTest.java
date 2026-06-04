package br.com.byop.aionlogbook.analytics.api;

import br.com.byop.aionlogbook.analytics.application.AnalyticsService;
import br.com.byop.aionlogbook.analytics.dto.AnalyticsOverviewResponse;
import br.com.byop.aionlogbook.analytics.dto.PlannedVsExecutedResponse;
import br.com.byop.aionlogbook.analytics.dto.PlansByDayResponse;
import br.com.byop.aionlogbook.analytics.dto.StatusDistributionResponse;
import br.com.byop.aionlogbook.analytics.dto.TimeByDirectionResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AnalyticsController.class,
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/realms/test"
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private Clock clock;

    @Test
    void shouldGetOverview() throws Exception {
        var userId = UUID.randomUUID();

        var response = new AnalyticsOverviewResponse(
                10,
                4,
                2,
                1,
                1,
                2,
                3,
                180,
                90,
                60.0
        );

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(analyticsService.getOverview(userId, null, null)).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlans").value(10))
                .andExpect(jsonPath("$.completedPlans").value(4))
                .andExpect(jsonPath("$.partialPlans").value(2))
                .andExpect(jsonPath("$.missedPlans").value(1))
                .andExpect(jsonPath("$.ignoredPlans").value(1))
                .andExpect(jsonPath("$.canceledPlans").value(2))
                .andExpect(jsonPath("$.activeDirections").value(3))
                .andExpect(jsonPath("$.executedMinutes").value(180))
                .andExpect(jsonPath("$.weeklyTimeMinutes").value(90))
                .andExpect(jsonPath("$.completionRate").value(60.0));

        verify(analyticsService).getOverview(userId, null, null);
    }

    @Test
    void shouldGetOverviewWithDateFilters() throws Exception {
        var userId = UUID.randomUUID();
        var dateFrom = LocalDate.of(2026, 6, 1);
        var dateTo = LocalDate.of(2026, 6, 30);

        var response = new AnalyticsOverviewResponse(
                1,
                1,
                0,
                0,
                0,
                0,
                1,
                45,
                45,
                100.0
        );

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(analyticsService.getOverview(userId, dateFrom, dateTo)).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .param("dateFrom", "2026-06-01")
                        .param("dateTo", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlans").value(1))
                .andExpect(jsonPath("$.completionRate").value(100.0));

        verify(analyticsService).getOverview(userId, dateFrom, dateTo);
    }

    @Test
    void shouldGetPlansByDay() throws Exception {
        var userId = UUID.randomUUID();

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(analyticsService.getPlansByDay(userId, null, null))
                .thenReturn(List.of(
                        new PlansByDayResponse(
                                "seg.",
                                LocalDate.of(2026, 6, 1),
                                5,
                                3
                        )
                ));

        mockMvc.perform(get("/api/v1/analytics/plans-by-day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].day").value("seg."))
                .andExpect(jsonPath("$[0].date").value("2026-06-01"))
                .andExpect(jsonPath("$[0].planned").value(5))
                .andExpect(jsonPath("$[0].executed").value(3));

        verify(analyticsService).getPlansByDay(userId, null, null);
    }

    @Test
    void shouldGetStatusDistribution() throws Exception {
        var userId = UUID.randomUUID();

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(analyticsService.getStatusDistribution(userId, null, null))
                .thenReturn(List.of(
                        new StatusDistributionResponse("COMPLETED", 3, 75.0),
                        new StatusDistributionResponse("PARTIAL", 1, 25.0)
                ));

        mockMvc.perform(get("/api/v1/analytics/status-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].total").value(3))
                .andExpect(jsonPath("$[0].percentage").value(75.0))
                .andExpect(jsonPath("$[1].status").value("PARTIAL"))
                .andExpect(jsonPath("$[1].total").value(1))
                .andExpect(jsonPath("$[1].percentage").value(25.0));

        verify(analyticsService).getStatusDistribution(userId, null, null);
    }

    @Test
    void shouldGetTimeByDirection() throws Exception {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(analyticsService.getTimeByDirection(userId, null, null))
                .thenReturn(List.of(
                        new TimeByDirectionResponse(
                                directionId,
                                "Código",
                                "#22c55e",
                                120,
                                80.0,
                                4
                        )
                ));

        mockMvc.perform(get("/api/v1/analytics/time-by-direction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].directionId").value(directionId.toString()))
                .andExpect(jsonPath("$[0].directionName").value("Código"))
                .andExpect(jsonPath("$[0].color").value("#22c55e"))
                .andExpect(jsonPath("$[0].totalMinutes").value(120))
                .andExpect(jsonPath("$[0].percentage").value(80.0))
                .andExpect(jsonPath("$[0].sessionsCount").value(4));

        verify(analyticsService).getTimeByDirection(userId, null, null);
    }

    @Test
    void shouldGetPlannedVsExecuted() throws Exception {
        var userId = UUID.randomUUID();

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(analyticsService.getPlannedVsExecuted(userId, null, null))
                .thenReturn(List.of(
                        new PlannedVsExecutedResponse(
                                "seg.",
                                LocalDate.of(2026, 6, 1),
                                120,
                                90
                        )
                ));

        mockMvc.perform(get("/api/v1/analytics/planned-vs-executed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].day").value("seg."))
                .andExpect(jsonPath("$[0].date").value("2026-06-01"))
                .andExpect(jsonPath("$[0].plannedMinutes").value(120))
                .andExpect(jsonPath("$[0].executedMinutes").value(90));

        verify(analyticsService).getPlannedVsExecuted(userId, null, null);
    }

    @Test
    void shouldPassDateFiltersToPlansByDay() throws Exception {
        var userId = UUID.randomUUID();
        var dateFrom = LocalDate.of(2026, 6, 1);
        var dateTo = LocalDate.of(2026, 6, 30);

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(analyticsService.getPlansByDay(userId, dateFrom, dateTo))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/analytics/plans-by-day")
                        .param("dateFrom", "2026-06-01")
                        .param("dateTo", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(analyticsService).getPlansByDay(userId, dateFrom, dateTo);
    }

    @Test
    void shouldReturnBadRequestWhenDateFilterIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/overview")
                        .param("dateFrom", "data-do-capeta"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analyticsService);
    }
}
