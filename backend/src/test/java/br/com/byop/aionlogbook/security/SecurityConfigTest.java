package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
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
