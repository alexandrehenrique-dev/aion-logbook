package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.analytics.application.AnalyticsService;
import br.com.byop.aionlogbook.analytics.infrastructure.AnalyticsRepository;
import br.com.byop.aionlogbook.bugreport.application.CreateBugReportUseCase;
import br.com.byop.aionlogbook.dashboard.application.DashboardService;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.logbook.application.LogEntryService;
import br.com.byop.aionlogbook.onboarding.application.OnboardingService;
import br.com.byop.aionlogbook.plan.application.CreatePlanUseCase;
import br.com.byop.aionlogbook.plan.application.GetPlanEventsUseCase;
import br.com.byop.aionlogbook.plan.application.GetPlanUseCase;
import br.com.byop.aionlogbook.plan.application.ListPlansUseCase;
import br.com.byop.aionlogbook.plan.application.TransitionPlanUseCase;
import br.com.byop.aionlogbook.plan.application.UpdatePlanUseCase;
import br.com.byop.aionlogbook.plan.infrastructure.PlanEventRepository;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.session.application.SessionLogService;
import br.com.byop.aionlogbook.settings.application.SettingsService;
import br.com.byop.aionlogbook.shared.time.TimeProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private TimeProvider timeProvider;

    @MockitoBean
    private DirectionRepository directionRepository;

    @MockitoBean
    private PlanRepository planRepository;

    @MockitoBean
    private PlanEventRepository planEventRepository;

    @MockitoBean
    private CreatePlanUseCase createPlanUseCase;

    @MockitoBean
    private UpdatePlanUseCase updatePlanUseCase;

    @MockitoBean
    private GetPlanUseCase getPlanUseCase;

    @MockitoBean
    private ListPlansUseCase listPlansUseCase;

    @MockitoBean
    private GetPlanEventsUseCase getPlanEventsUseCase;

    @MockitoBean
    private TransitionPlanUseCase transitionPlanUseCase;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private SessionLogService sessionLogService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private CreateBugReportUseCase createBugReportUseCase;

    @MockitoBean
    private OnboardingService onboardingService;

    @MockitoBean
    private SettingsService settingsService;

    @MockitoBean
    private LogEntryService logEntryService;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private AnalyticsRepository analyticsRepository;

    @Nested
    class ApiV1 {

        @Test
        void shouldReturnUnauthorizedWhenCallingMeWithoutToken() throws Exception {
            when(timeProvider.now()).thenReturn(OffsetDateTime.parse("2026-06-02T12:00:00Z"));

            mockMvc.perform(get("/api/v1/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnCurrentProfileWhenCallingMeWithJwt() throws Exception {
            UserProfile profile = new UserProfile(
                    "sub-123",
                    "loki@byop.com",
                    "loki",
                    "Loki"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            mockMvc.perform(get("/api/v1/me")
                            .with(jwt().jwt(token -> token
                                    .subject("sub-123")
                                    .claim("email", "loki@byop.com")
                                    .claim("preferred_username", "loki")
                                    .claim("name", "Loki")
                            )))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.keycloakSubject", is("sub-123")))
                    .andExpect(jsonPath("$.email", is("loki@byop.com")))
                    .andExpect(jsonPath("$.username", is("loki")))
                    .andExpect(jsonPath("$.fullName", is("Loki")));
        }
    }

    @Nested
    class PublicRoutes {

        @Test
        void shouldNotRequireAuthenticationForHealth() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus())
                            .isNotIn(401, 403));
        }

        @Test
        void shouldNotRequireAuthenticationForStaticIndex() throws Exception {
            mockMvc.perform(get("/index.html"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus())
                            .isNotIn(401, 403));
        }

        @Test
        void shouldNotRequireAuthenticationForSwaggerInLocalProfile() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(result -> assertThat(result.getResponse().getStatus())
                            .isNotIn(401, 403));
        }
    }
}
