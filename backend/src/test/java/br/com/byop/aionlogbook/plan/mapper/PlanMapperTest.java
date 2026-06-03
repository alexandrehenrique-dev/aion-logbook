package br.com.byop.aionlogbook.plan.mapper;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.dto.CreatePlanRequest;
import br.com.byop.aionlogbook.plan.dto.UpdatePlanRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlanMapperTest {

    private final PlanMapper mapper = new PlanMapper();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        void shouldMapCreateRequestToPlan() {
            var userId = UUID.randomUUID();
            var directionId = UUID.randomUUID();
            var now = Instant.parse("2026-06-03T12:00:00Z");
            var plannedStartAt = Instant.parse("2026-06-04T12:00:00Z");

            var request = new CreatePlanRequest(
                    directionId,
                    "Estudar Spring",
                    "Estudar mapper",
                    "STUDY",
                    Priority.HIGH,
                    LocalDate.parse("2026-06-04"),
                    plannedStartAt,
                    60,
                    true,
                    Instant.parse("2026-06-04T11:45:00Z"),
                    "Motivo teste",
                    List.of("java", "spring")
            );

            var result = mapper.toEntity(request, userId, now);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getDirectionId()).isEqualTo(directionId);
            assertThat(result.getTitle()).isEqualTo("Estudar Spring");
            assertThat(result.getDescription()).isEqualTo("Estudar mapper");
            assertThat(result.getType()).isEqualTo("STUDY");
            assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
            assertThat(result.getPlannedDate()).isEqualTo(LocalDate.parse("2026-06-04"));
            assertThat(result.getPlannedStartAt()).isEqualTo(plannedStartAt);
            assertThat(result.getEstimatedMinutes()).isEqualTo(60);
            assertThat(result.isNotify()).isTrue();
            assertThat(result.getReason()).isEqualTo("Motivo teste");
            assertThat(result.getTags()).containsExactly("java", "spring");
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            assertThat(result.getLastStatusChangedAt()).isEqualTo(now);
        }

        @Test
        void shouldUseMediumPriorityWhenRequestPriorityIsNull() {
            var userId = UUID.randomUUID();
            var now = Instant.parse("2026-06-03T12:00:00Z");

            var request = new CreatePlanRequest(
                    null,
                    "Plano",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null
            );

            var result = mapper.toEntity(request, userId, now);

            assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
        }
    }

    @Nested
    @DisplayName("applyUpdate")
    class ApplyUpdate {

        @Test
        void shouldApplyOnlyProvidedFields() {
            var plan = basePlan();
            var now = Instant.parse("2026-06-03T15:00:00Z");
            var newDirectionId = UUID.randomUUID();

            var request = new UpdatePlanRequest(
                    newDirectionId,
                    "Novo título",
                    null,
                    "WORK",
                    Priority.CRITICAL,
                    null,
                    Instant.parse("2026-06-05T10:00:00Z"),
                    90,
                    true,
                    null,
                    "Novo motivo",
                    List.of("deep-work")
            );

            mapper.applyUpdate(plan, request, now);

            assertThat(plan.getDirectionId()).isEqualTo(newDirectionId);
            assertThat(plan.getTitle()).isEqualTo("Novo título");
            assertThat(plan.getDescription()).isEqualTo("Descrição antiga");
            assertThat(plan.getType()).isEqualTo("WORK");
            assertThat(plan.getPriority()).isEqualTo(Priority.CRITICAL);
            assertThat(plan.getPlannedDate()).isEqualTo(LocalDate.parse("2026-06-03"));
            assertThat(plan.getPlannedStartAt()).isEqualTo(Instant.parse("2026-06-05T10:00:00Z"));
            assertThat(plan.getEstimatedMinutes()).isEqualTo(90);
            assertThat(plan.isNotify()).isTrue();
            assertThat(plan.getNotificationDateTime()).isNull();
            assertThat(plan.getReason()).isEqualTo("Novo motivo");
            assertThat(plan.getTags()).containsExactly("deep-work");
            assertThat(plan.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        void shouldPreserveFieldsWhenUpdateRequestIsEmpty() {
            var plan = basePlan();
            var originalTitle = plan.getTitle();
            var originalUpdatedAt = plan.getUpdatedAt();
            var now = Instant.parse("2026-06-03T16:00:00Z");

            var request = new UpdatePlanRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            mapper.applyUpdate(plan, request, now);

            assertThat(plan.getTitle()).isEqualTo(originalTitle);
            assertThat(plan.getUpdatedAt()).isNotEqualTo(originalUpdatedAt);
            assertThat(plan.getUpdatedAt()).isEqualTo(now);
        }
    }

    @Test
    void shouldMapPlanToResponse() {
        var plan = basePlan();

        var response = mapper.toResponse(plan);

        assertThat(response.id()).isEqualTo(plan.getId());
        assertThat(response.directionId()).isEqualTo(plan.getDirectionId());
        assertThat(response.title()).isEqualTo(plan.getTitle());
        assertThat(response.description()).isEqualTo(plan.getDescription());
        assertThat(response.priority()).isEqualTo(plan.getPriority());
        assertThat(response.status()).isEqualTo(plan.getStatus());
        assertThat(response.tags()).containsExactly("tag-antiga");
    }

    @Test
    void shouldMapPlanEventToResponse() {
        var event = new PlanEvent();
        var id = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var createdAt = Instant.parse("2026-06-03T18:00:00Z");

        event.setId(id);
        event.setPlanId(planId);
        event.setUserId(userId);
        event.setEventType(PlanEventType.CREATED);
        event.setFromStatus(null);
        event.setToStatus(PlanStatus.DRAFT);
        event.setDescription("Plano criado");
        event.setMetadata(Map.of("source", "test"));
        event.setCreatedAt(createdAt);

        var response = mapper.toEventResponse(event);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.planId()).isEqualTo(planId);
        assertThat(response.eventType()).isEqualTo(PlanEventType.CREATED);
        assertThat(response.fromStatus()).isNull();
        assertThat(response.toStatus()).isEqualTo(PlanStatus.DRAFT);
        assertThat(response.description()).isEqualTo("Plano criado");
        assertThat(response.metadata()).containsEntry("source", "test");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    private static Plan basePlan() {
        var now = Instant.parse("2026-06-03T12:00:00Z");

        var plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setUserId(UUID.randomUUID());
        plan.setDirectionId(UUID.randomUUID());
        plan.setTitle("Título antigo");
        plan.setDescription("Descrição antiga");
        plan.setType("OLD");
        plan.setPriority(Priority.MEDIUM);
        plan.setStatus(PlanStatus.DRAFT);
        plan.setPlannedDate(LocalDate.parse("2026-06-03"));
        plan.setPlannedStartAt(null);
        plan.setPlannedEndAt(null);
        plan.setEstimatedMinutes(30);
        plan.setNotify(false);
        plan.setNotificationDateTime(null);
        plan.setStartedAt(null);
        plan.setFinishedAt(null);
        plan.setActualMinutes(null);
        plan.setReason("Motivo antigo");
        plan.setTags(List.of("tag-antiga"));
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        return plan;
    }
}