package br.com.byop.aionlogbook.bugreport.api;

import br.com.byop.aionlogbook.bugreport.application.CreateBugReportUseCase;
import br.com.byop.aionlogbook.bugreport.domain.BugReportSeverity;
import br.com.byop.aionlogbook.bugreport.domain.BugReportStatus;
import br.com.byop.aionlogbook.bugreport.dto.BugReportResponse;
import br.com.byop.aionlogbook.bugreport.dto.CreateBugReportRequest;
import br.com.byop.aionlogbook.security.CurrentUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BugReportController.class,
        properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/realms/test"
)
class BugReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateBugReportUseCase createBugReportUseCase;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private Clock clock;

    @Nested
    class Create {

        @Test
        void shouldCreateBugReportWhenAuthenticated() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID bugReportId = UUID.randomUUID();

            BugReportResponse response = new BugReportResponse(
                    bugReportId,
                    BugReportStatus.RECEIVED,
                    false,
                    "Bug report registrado com sucesso."
            );

            when(currentUserService.currentUserId()).thenReturn(userId);
            when(createBugReportUseCase.execute(eq(userId), any(CreateBugReportRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/bug-reports")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(bugReportId.toString()))
                    .andExpect(jsonPath("$.status").value("RECEIVED"))
                    .andExpect(jsonPath("$.telegramSent").value(false))
                    .andExpect(jsonPath("$.message").value("Bug report registrado com sucesso."));

            verify(currentUserService).currentUserId();
            verify(createBugReportUseCase).execute(eq(userId), any(CreateBugReportRequest.class));
        }

        @Test
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
            mockMvc.perform(post("/api/v1/bug-reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnBadRequestWhenTitleIsTooShort() throws Exception {
            CreateBugReportRequest request = new CreateBugReportRequest(
                    "Erro",
                    "Ao clicar no botão salvar direção, nada acontece na tela.",
                    BugReportSeverity.HIGH,
                    "/directions/new",
                    Map.of()
            );

            mockMvc.perform(post("/api/v1/bug-reports")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenDescriptionIsTooShort() throws Exception {
            CreateBugReportRequest request = new CreateBugReportRequest(
                    "Erro ao salvar direção",
                    "curta",
                    BugReportSeverity.HIGH,
                    "/directions/new",
                    Map.of()
            );

            mockMvc.perform(post("/api/v1/bug-reports")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenSeverityIsNull() throws Exception {
            String payload = """
                    {
                      "title": "Erro ao salvar direção",
                      "description": "Ao clicar no botão salvar direção, nada acontece na tela.",
                      "severity": null,
                      "page": "/directions/new",
                      "metadata": {}
                    }
                    """;

            mockMvc.perform(post("/api/v1/bug-reports")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenPageIsTooLong() throws Exception {
            String longPage = "/" + "a".repeat(501);

            CreateBugReportRequest request = new CreateBugReportRequest(
                    "Erro ao salvar direção",
                    "Ao clicar no botão salvar direção, nada acontece na tela.",
                    BugReportSeverity.HIGH,
                    longPage,
                    Map.of()
            );

            mockMvc.perform(post("/api/v1/bug-reports")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private CreateBugReportRequest validRequest() {
            return new CreateBugReportRequest(
                    "Erro ao salvar direção",
                    "Ao clicar no botão salvar direção, nada acontece na tela.",
                    BugReportSeverity.HIGH,
                    "/directions/new",
                    Map.of("browser", "Chrome")
            );
        }
    }
}