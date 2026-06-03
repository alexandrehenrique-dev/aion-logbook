package br.com.byop.aionlogbook.plan.api;

import br.com.byop.aionlogbook.plan.application.*;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.dto.PlanEventResponse;
import br.com.byop.aionlogbook.plan.dto.PlanResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanController.class)
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private CurrentUserService currentUserService;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.parse("2026-06-03T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("America/Sao_Paulo"));
    }

    @Test
    void shouldListPlans() throws Exception {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var plannedDate = LocalDate.parse("2026-06-03");
        var response = planResponse(UUID.randomUUID(), directionId, PlanStatus.DUE);

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(listPlansUseCase.execute(
                eq(userId),
                eq(PlanStatus.DUE),
                eq(directionId),
                eq(plannedDate),
                any()
        )).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/plans")
                        .with(jwt())
                        .param("status", "DUE")
                        .param("directionId", directionId.toString())
                        .param("plannedDate", "2026-06-03")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                .andExpect(jsonPath("$.content[0].status").value("DUE"))
                .andExpect(jsonPath("$.content[0].directionId").value(directionId.toString()));

        verify(listPlansUseCase).execute(
                eq(userId),
                eq(PlanStatus.DUE),
                eq(directionId),
                eq(plannedDate),
                any()
        );
    }

    @Test
    void shouldCreatePlan() throws Exception {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var response = planResponse(planId, null, PlanStatus.DRAFT);

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(createPlanUseCase.execute(eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/plans")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Plano de teste",
                                  "description": "Descrição de teste",
                                  "type": "STUDY",
                                  "priority": "HIGH",
                                  "plannedDate": "2026-06-03",
                                  "estimatedMinutes": 60,
                                  "notificationEnabled": true,
                                  "tags": ["java", "spring"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.title").value("Plano de teste"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(createPlanUseCase).execute(eq(userId), any());
    }

    @Test
    void shouldGetPlanById() throws Exception {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var response = planResponse(planId, null, PlanStatus.SCHEDULED);

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(getPlanUseCase.execute(userId, planId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/plans/{id}", planId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(getPlanUseCase).execute(userId, planId);
    }

    @Test
    void shouldUpdatePlan() throws Exception {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var response = planResponse(planId, null, PlanStatus.DUE);

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(updatePlanUseCase.execute(eq(userId), eq(planId), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/plans/{id}", planId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Plano atualizado",
                                  "priority": "CRITICAL",
                                  "estimatedMinutes": 90,
                                  "notificationEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.status").value("DUE"));

        verify(updatePlanUseCase).execute(eq(userId), eq(planId), any());
    }

    @Test
    void shouldListPlanEvents() throws Exception {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        var eventResponse = new PlanEventResponse(
                eventId,
                planId,
                PlanEventType.CREATED,
                null,
                PlanStatus.DRAFT,
                "Plan created",
                Map.of("source", "test"),
                Instant.parse("2026-06-03T12:00:00Z")
        );

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(getPlanEventsUseCase.execute(userId, planId)).thenReturn(List.of(eventResponse));

        mockMvc.perform(get("/api/v1/plans/{id}/events", planId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$[0].planId").value(planId.toString()))
                .andExpect(jsonPath("$[0].eventType").value("CREATED"))
                .andExpect(jsonPath("$[0].toStatus").value("DRAFT"));

        verify(getPlanEventsUseCase).execute(userId, planId);
    }

    @Test
    void shouldRejectInvalidCreateRequestWithoutTitle() throws Exception {
        mockMvc.perform(post("/api/v1/plans")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Sem título"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private static PlanResponse planResponse(
            UUID planId,
            UUID directionId,
            PlanStatus status
    ) {
        return new PlanResponse(
                planId,
                directionId,
                "Plano de teste",
                "Descrição de teste",
                "STUDY",
                Priority.HIGH,
                status,
                LocalDate.parse("2026-06-03"),
                Instant.parse("2026-06-03T12:00:00Z"),
                Instant.parse("2026-06-03T13:00:00Z"),
                60,
                true,
                null,
                null,
                null,
                null,
                "Motivo teste",
                List.of("java", "spring"),
                Instant.parse("2026-06-03T10:00:00Z"),
                Instant.parse("2026-06-03T10:00:00Z"),
                Instant.parse("2026-06-03T10:00:00Z")
        );
    }
}