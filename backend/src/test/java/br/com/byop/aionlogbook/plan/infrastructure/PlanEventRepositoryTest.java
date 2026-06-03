package br.com.byop.aionlogbook.plan.infrastructure;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.PlanEventType;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.support.PostgresRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlanEventRepositoryTest extends PostgresRepositoryTest {

    @Autowired
    private PlanEventRepository repository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("findByPlanIdAndUserIdOrderByCreatedAtAsc")
    class FindByPlanIdAndUserIdOrderByCreatedAtAsc {

        @Test
        void shouldListEventsInChronologicalOrder() {
            var userId = createUserProfile();
            var plan = planRepository.save(plan(userId));
            var planId = plan.getId();

            var second = repository.save(event(
                    userId,
                    planId,
                    PlanEventType.UPDATED,
                    Instant.parse("2026-06-02T10:05:00Z")
            ));

            var first = repository.save(event(
                    userId,
                    planId,
                    PlanEventType.CREATED,
                    Instant.parse("2026-06-02T10:00:00Z")
            ));

            var result = repository.findByPlanIdAndUserIdOrderByCreatedAtAsc(planId, userId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(first.getId());
            assertThat(result.get(1).getId()).isEqualTo(second.getId());
        }

        @Test
        void shouldNotListEventsFromAnotherUser() {
            var ownerId = createUserProfile();
            var anotherUserId = createUserProfile();

            var plan = planRepository.save(plan(ownerId));
            var planId = plan.getId();

            repository.save(event(
                    ownerId,
                    planId,
                    PlanEventType.CREATED,
                    Instant.now()
            ));

            var result = repository.findByPlanIdAndUserIdOrderByCreatedAtAsc(
                    planId,
                    anotherUserId
            );

            assertThat(result).isEmpty();
        }
    }

    private UUID createUserProfile() {
        var id = UUID.randomUUID();
        var keycloakSubject = "test-subject-" + id;

        jdbcTemplate.update("""
                INSERT INTO user_profiles (
                    id,
                    keycloak_subject,
                    email,
                    username,
                    full_name,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, now(), now())
                """,
                id,
                keycloakSubject,
                id + "@test.local",
                "user_" + id.toString().substring(0, 8),
                "Usuário Teste"
        );

        return id;
    }

    private static Plan plan(UUID userId) {
        var now = Instant.now();

        var plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setUserId(userId);
        plan.setDirectionId(null);
        plan.setTitle("Plano de teste");
        plan.setDescription("Descrição de teste");
        plan.setPriority(Priority.MEDIUM);
        plan.setStatus(PlanStatus.DRAFT);
        plan.setPlannedDate(LocalDate.now());
        plan.setNotify(false);
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);
        plan.setTags(null);

        return plan;
    }

    private static PlanEvent event(
            UUID userId,
            UUID planId,
            PlanEventType type,
            Instant createdAt
    ) {
        var event = new PlanEvent();
        event.setId(UUID.randomUUID());
        event.setUserId(userId);
        event.setPlanId(planId);
        event.setEventType(type);
        event.setFromStatus(null);
        event.setToStatus(PlanStatus.DRAFT);
        event.setDescription("Evento de teste");
        event.setMetadata(null);
        event.setCreatedAt(createdAt);

        return event;
    }
}