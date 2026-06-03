package br.com.byop.aionlogbook.plan.infrastructure;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.support.PostgresRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlanRepositoryTest extends PostgresRepositoryTest {

    @Autowired
    private PlanRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        void shouldFindPlanWhenBelongsToUser() {
            var userId = createUserProfile();
            var plan = repository.save(plan(userId));

            var result = repository.findByIdAndUserId(plan.getId(), userId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(plan.getId());
        }

        @Test
        void shouldNotFindPlanFromAnotherUser() {
            var ownerId = createUserProfile();
            var anotherUserId = createUserProfile();

            var plan = repository.save(plan(ownerId));

            var result = repository.findByIdAndUserId(plan.getId(), anotherUserId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("filters")
    class Filters {

        @Test
        void shouldFindByUserId() {
            var userId = createUserProfile();
            var anotherUserId = createUserProfile();

            repository.save(plan(userId));
            repository.save(plan(anotherUserId));

            var result = repository.findByUserId(userId, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void shouldFindByUserIdAndStatus() {
            var userId = createUserProfile();

            repository.save(plan(userId, PlanStatus.DRAFT, null, null));
            repository.save(plan(userId, PlanStatus.DUE, null, null));

            var result = repository.findByUserIdAndStatus(
                    userId,
                    PlanStatus.DUE,
                    PageRequest.of(0, 10)
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getStatus()).isEqualTo(PlanStatus.DUE);
        }

        @Test
        void shouldFindByUserIdAndDirectionId() {
            var userId = createUserProfile();
            var directionId = createDirection(userId);
            var anotherDirectionId = createDirection(userId);

            repository.save(plan(userId, PlanStatus.DRAFT, directionId, null));
            repository.save(plan(userId, PlanStatus.DRAFT, anotherDirectionId, null));

            var result = repository.findByUserIdAndDirectionId(
                    userId,
                    directionId,
                    PageRequest.of(0, 10)
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getDirectionId()).isEqualTo(directionId);
        }

        @Test
        void shouldFindByUserIdAndPlannedDate() {
            var userId = createUserProfile();
            var plannedDate = LocalDate.now();

            repository.save(plan(userId, PlanStatus.PENDING, null, plannedDate));
            repository.save(plan(userId, PlanStatus.PENDING, null, plannedDate.plusDays(1)));

            var result = repository.findByUserIdAndPlannedDate(
                    userId,
                    plannedDate,
                    PageRequest.of(0, 10)
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getPlannedDate()).isEqualTo(plannedDate);
        }

        @Test
        void shouldFindByAllFilters() {
            var userId = createUserProfile();
            var directionId = createDirection(userId);
            var anotherDirectionId = createDirection(userId);
            var plannedDate = LocalDate.now();

            repository.save(plan(userId, PlanStatus.DUE, directionId, plannedDate));
            repository.save(plan(userId, PlanStatus.DRAFT, directionId, plannedDate));
            repository.save(plan(userId, PlanStatus.DUE, anotherDirectionId, plannedDate));
            repository.save(plan(userId, PlanStatus.DUE, directionId, plannedDate.plusDays(1)));

            var result = repository.findByUserIdAndStatusAndDirectionIdAndPlannedDate(
                    userId,
                    PlanStatus.DUE,
                    directionId,
                    plannedDate,
                    PageRequest.of(0, 10)
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
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

    private UUID createDirection(UUID userId) {
        var id = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO directions (
                    id,
                    user_profile_id,
                    name,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, now(), now())
                """,
                id,
                userId,
                "Direção Teste " + id.toString().substring(0, 8),
                "ACTIVE"
        );

        return id;
    }

    private static Plan plan(UUID userId) {
        return plan(userId, PlanStatus.DRAFT, null, null);
    }

    private static Plan plan(
            UUID userId,
            PlanStatus status,
            UUID directionId,
            LocalDate plannedDate
    ) {
        var now = Instant.now();

        var plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setUserId(userId);
        plan.setDirectionId(directionId);
        plan.setTitle("Plano de teste");
        plan.setDescription("Descrição de teste");
        plan.setPriority(Priority.MEDIUM);
        plan.setStatus(status);
        plan.setPlannedDate(plannedDate);
        plan.setNotify(false);
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);
        plan.setTags(null);

        return plan;
    }
}