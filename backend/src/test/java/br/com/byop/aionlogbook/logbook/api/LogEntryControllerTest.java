package br.com.byop.aionlogbook.logbook.api;

import br.com.byop.aionlogbook.logbook.application.LogEntryService;
import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import br.com.byop.aionlogbook.logbook.dto.CreateLogEntryRequest;
import br.com.byop.aionlogbook.logbook.dto.LogEntryResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@WebMvcTest(
        controllers = LogEntryController.class,
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/realms/test"
        }
)
@AutoConfigureMockMvc(addFilters = false)
class LogEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogEntryService logEntryService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private Clock clock;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateLogEntry() throws Exception {
        var userId = UUID.randomUUID();
        var id = UUID.randomUUID();

        var request = new CreateLogEntryRequest(
                null,
                null,
                "Título",
                "Conteúdo",
                LogEntryType.REFLECTION,
                List.of("tag")
        );

        var response = new LogEntryResponse(
                id,
                null,
                null,
                "Título",
                "Conteúdo",
                LogEntryType.REFLECTION,
                List.of("tag"),
                Instant.parse("2026-06-04T10:00:00Z"),
                Instant.parse("2026-06-04T10:00:00Z")
        );

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(logEntryService.create(userId, request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Título"))
                .andExpect(jsonPath("$.content").value("Conteúdo"))
                .andExpect(jsonPath("$.type").value("REFLECTION"));
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        var request = new CreateLogEntryRequest(
                null,
                null,
                "",
                "",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(logEntryService);
    }

    @Test
    void shouldGetById() throws Exception {
        var userId = UUID.randomUUID();
        var id = UUID.randomUUID();

        var response = new LogEntryResponse(
                id,
                null,
                null,
                "Título",
                "Conteúdo",
                LogEntryType.IDEA,
                List.of("tag"),
                Instant.parse("2026-06-04T10:00:00Z"),
                Instant.parse("2026-06-04T10:00:00Z")
        );

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(logEntryService.getById(userId, id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/logs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("IDEA"));
    }

    @Test
    void shouldDelete() throws Exception {
        var userId = UUID.randomUUID();
        var id = UUID.randomUUID();

        when(currentUserService.currentUserId()).thenReturn(userId);

        mockMvc.perform(delete("/api/v1/logs/{id}", id))
                .andExpect(status().isNoContent());

        verify(logEntryService).delete(userId, id);
    }
}
