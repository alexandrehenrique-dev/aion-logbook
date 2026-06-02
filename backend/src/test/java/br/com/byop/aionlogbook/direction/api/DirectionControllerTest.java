package br.com.byop.aionlogbook.direction.api;

import br.com.byop.aionlogbook.direction.application.DirectionService;
import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.dto.CreateDirectionRequest;
import br.com.byop.aionlogbook.direction.dto.UpdateDirectionRequest;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.shared.error.DirectionNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DirectionController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class DirectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DirectionService service;

    @MockitoBean
    private Clock clock;

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-06-02T10:00:00Z");

    @Nested
    class FindAll {

        @Test
        void shouldReturnDirections() throws Exception {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            when(service.findAll(null)).thenReturn(List.of(direction));

            mockMvc.perform(get("/api/v1/directions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(direction.getId().toString()))
                    .andExpect(jsonPath("$[0].name").value("Carreira"))
                    .andExpect(jsonPath("$[0].description").value("Direção profissional"))
                    .andExpect(jsonPath("$[0].color").value("#00FF99"))
                    .andExpect(jsonPath("$[0].icon").value("compass"))
                    .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$[0].identityPhrase").value("Construir com presença"));
        }

        @Test
        void shouldReturnArchivedDirectionsWhenStatusIsProvided() throws Exception {
            when(service.findAll(DirectionStatus.ARCHIVED)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/directions")
                            .param("status", "ARCHIVED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnDirectionById() throws Exception {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            when(service.findById(direction.getId())).thenReturn(direction);

            mockMvc.perform(get("/api/v1/directions/{id}", direction.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(direction.getId().toString()))
                    .andExpect(jsonPath("$.name").value("Carreira"));
        }

        @Test
        void shouldReturnNotFoundWhenDirectionDoesNotBelongToCurrentUser() throws Exception {
            UUID directionId = UUID.randomUUID();

            when(service.findById(directionId)).thenThrow(new DirectionNotFoundException());

            mockMvc.perform(get("/api/v1/directions/{id}", directionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Direção não encontrada."));
        }
    }

    @Nested
    class Create {

        @Test
        void shouldCreateDirection() throws Exception {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            CreateDirectionRequest request = new CreateDirectionRequest(
                    "Carreira",
                    "Direção profissional",
                    "#00FF99",
                    "compass",
                    "Construir com presença"
            );

            when(service.create(request)).thenReturn(direction);

            mockMvc.perform(post("/api/v1/directions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(direction.getId().toString()))
                    .andExpect(jsonPath("$.name").value("Carreira"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
            CreateDirectionRequest request = new CreateDirectionRequest(
                    "",
                    "Direção profissional",
                    "#00FF99",
                    "compass",
                    "Construir com presença"
            );

            mockMvc.perform(post("/api/v1/directions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenColorIsInvalid() throws Exception {
            CreateDirectionRequest request = new CreateDirectionRequest(
                    "Carreira",
                    "Direção profissional",
                    "verde-matrix",
                    "compass",
                    "Construir com presença"
            );

            mockMvc.perform(post("/api/v1/directions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateDirection() throws Exception {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            UpdateDirectionRequest request = new UpdateDirectionRequest(
                    "Espiritualidade",
                    "Direção interior",
                    "#FFFFFF",
                    "lotus",
                    "Conhece-te a ti mesmo"
            );

            direction.update(
                    request.name(),
                    request.description(),
                    request.color(),
                    request.icon(),
                    request.identityPhrase(),
                    NOW
            );

            when(service.update(direction.getId(), request)).thenReturn(direction);

            mockMvc.perform(put("/api/v1/directions/{id}", direction.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Espiritualidade"))
                    .andExpect(jsonPath("$.description").value("Direção interior"))
                    .andExpect(jsonPath("$.color").value("#FFFFFF"))
                    .andExpect(jsonPath("$.icon").value("lotus"))
                    .andExpect(jsonPath("$.identityPhrase").value("Conhece-te a ti mesmo"));
        }
    }

    @Nested
    class Archive {

        @Test
        void shouldArchiveDirection() throws Exception {
            UUID directionId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/directions/{id}", directionId))
                    .andExpect(status().isNoContent());

            verify(service).archive(directionId);
        }

        @Test
        void shouldReturnNotFoundWhenArchivingDirectionFromAnotherUser() throws Exception {
            UUID directionId = UUID.randomUUID();

            doThrow(new DirectionNotFoundException()).when(service).archive(directionId);

            mockMvc.perform(delete("/api/v1/directions/{id}", directionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        }
    }

    private static Direction direction(UserProfile userProfile) {
        return new Direction(
                userProfile,
                "Carreira",
                "Direção profissional",
                "#00FF99",
                "compass",
                "Construir com presença",
                NOW
        );
    }

    private static UserProfile userProfile() {
        return Mockito.mock(UserProfile.class);
    }
}
