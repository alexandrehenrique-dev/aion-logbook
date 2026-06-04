package br.com.byop.aionlogbook.analytics.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(AnalyticsRepository.class)
class AnalyticsRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AnalyticsRepository repository;

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("aion_logbook_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Test
    void shouldReturnZeroOverviewWhenDatabaseIsEmptyForUser() {
        var userId = createUserProfile();

        var result = repository.overview(userId, null, null);

        assertThat(result).satisfies(r -> {
            assertThat(r.plansCreated()).isZero();
            assertThat(r.plansCompleted()).isZero();
            assertThat(r.plansPartial()).isZero();
            assertThat(r.totalTimeMinutes()).isZero();
            assertThat(r.weeklyTimeMinutes()).isZero();
        });
    }

    @Test
    void shouldCalculateOverviewOnlyForOwner() {
        var userId = createUserProfile();
        var anotherUserId = createUserProfile();

        createPlan(userId, "COMPLETED", LocalDate.now(), 60, null);
        createPlan(userId, "PARTIAL", LocalDate.now(), 30, null);
        createPlan(userId, "MISSED", LocalDate.now(), 45, null);

        createPlan(anotherUserId, "COMPLETED", LocalDate.now(), 999, null);
        createSessionLog(userId, null, null, Timestamp.from(Instant.now()), 50);
        createSessionLog(anotherUserId, null, null, Timestamp.from(Instant.now()), 999);

        var result = repository.overview(userId, null, null);

        assertThat(result).satisfies(r -> {
            assertThat(r.plansCreated()).isEqualTo(3);
            assertThat(r.plansCompleted()).isEqualTo(1);
            assertThat(r.plansPartial()).isEqualTo(1);
            assertThat(r.plansMissed()).isEqualTo(1);
            assertThat(r.totalTimeMinutes()).isEqualTo(50);
        });
    }

    @Test
    void shouldFilterOverviewByDateRange() {
        var userId = createUserProfile();

        createPlan(userId, "COMPLETED", LocalDate.of(2026, 6, 1), 60, null);
        createPlan(userId, "MISSED", LocalDate.of(2026, 6, 10), 60, null);

        createSessionLog(userId, null, null, Timestamp.from(Instant.parse("2026-06-01T10:00:00Z")), 40);
        createSessionLog(userId, null, null, Timestamp.from(Instant.parse("2026-06-10T10:00:00Z")), 80);

        var result = repository.overview(
                userId,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1)
        );

        assertThat(result).satisfies(r -> {
            assertThat(r.plansCreated()).isEqualTo(1);
            assertThat(r.plansCompleted()).isEqualTo(1);
            assertThat(r.totalTimeMinutes()).isEqualTo(40);
        });
    }

    @Test
    void shouldReturnPlansByDay() {
        var userId = createUserProfile();

        createPlan(userId, "COMPLETED", LocalDate.of(2026, 6, 1), 60, null);
        createPlan(userId, "PARTIAL", LocalDate.of(2026, 6, 1), 30, null);
        createPlan(userId, "MISSED", LocalDate.of(2026, 6, 2), 45, null);

        var result = repository.plansByDay(userId, null, null);

        assertThat(result).hasSize(2);

        assertThat(result.getFirst()).satisfies(first -> {
            assertThat(first.date()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(first.planned()).isEqualTo(2);
            assertThat(first.executed()).isEqualTo(2);
        });

        assertThat(result.get(1)).satisfies(second -> {
            assertThat(second.date()).isEqualTo(LocalDate.of(2026, 6, 2));
            assertThat(second.planned()).isEqualTo(1);
            assertThat(second.executed()).isZero();
        });
    }

    @Test
    void shouldReturnStatusDistribution() {
        var userId = createUserProfile();

        createPlan(userId, "COMPLETED", LocalDate.now(), 60, null);
        createPlan(userId, "COMPLETED", LocalDate.now(), 60, null);
        createPlan(userId, "PARTIAL", LocalDate.now(), 30, null);

        var result = repository.statusDistribution(userId, null, null);

        assertThat(result)
                .hasSize(2)
                .anyMatch(item -> item.status().equals("COMPLETED") && item.count() == 2)
                .anyMatch(item -> item.status().equals("PARTIAL") && item.count() == 1);
    }

    @Test
    void shouldReturnEmptyStatusDistributionWhenNoPlans() {
        var userId = createUserProfile();

        var result = repository.statusDistribution(userId, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTimeByDirection() {
        var userId = createUserProfile();
        var directionId = createDirection(userId, "Código", "#22c55e");

        createSessionLog(userId, null, directionId, Timestamp.from(Instant.now()), 40);
        createSessionLog(userId, null, directionId, Timestamp.from(Instant.now()), 20);

        var result = repository.timeByDirection(userId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).satisfies(first -> {
            assertThat(first.directionId()).isEqualTo(directionId);
            assertThat(first.directionName()).isEqualTo("Código");
            assertThat(first.color()).isEqualTo("#22c55e");
            assertThat(first.totalMinutes()).isEqualTo(60);
            assertThat(first.sessionsCount()).isEqualTo(2);
        });
    }

    @Test
    void shouldReturnEmptyTimeByDirectionWhenNoSessions() {
        var userId = createUserProfile();

        var result = repository.timeByDirection(userId, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnPlannedVsExecuted() {
        var userId = createUserProfile();

        createPlan(userId, "SCHEDULED", LocalDate.of(2026, 6, 1), 60, null);
        createPlan(userId, "SCHEDULED", LocalDate.of(2026, 6, 1), 30, null);
        createSessionLog(userId, null, null, Timestamp.from(Instant.parse("2026-06-01T10:00:00Z")), 45);

        var result = repository.plannedVsExecuted(userId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).satisfies(first -> {
            assertThat(first.date()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(first.plannedMinutes()).isEqualTo(90);
            assertThat(first.executedMinutes()).isEqualTo(45);
        });
    }

    @Test
    void shouldReturnExecutedEvenWithoutPlans() {
        var userId = createUserProfile();

        createSessionLog(userId, null, null, Timestamp.from(Instant.parse("2026-06-01T10:00:00Z")), 25);

        var result = repository.plannedVsExecuted(userId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).satisfies(first -> {
            assertThat(first.plannedMinutes()).isZero();
            assertThat(first.executedMinutes()).isEqualTo(25);
        });
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

    private UUID createDirection(UUID userId, String name, String color) {
        var id = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO directions (
                    id,
                    user_profile_id,
                    name,
                    color,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, now(), now())
                """,
                id,
                userId,
                name,
                color,
                "ACTIVE"
        );

        return id;
    }

    private UUID createPlan(
            UUID userId,
            String status,
            LocalDate plannedDate,
            Integer estimatedMinutes,
            UUID directionId
    ) {
        var id = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO plans (
                    id,
                    user_id,
                    direction_id,
                    title,
                    description,
                    priority,
                    status,
                    planned_date,
                    estimated_minutes,
                    notify,
                    created_at,
                    updated_at,
                    last_status_changed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, false, now(), now(), now())
                """,
                id,
                userId,
                directionId,
                "Plano de teste",
                "Descrição de teste",
                "MEDIUM",
                status,
                plannedDate,
                estimatedMinutes
        );

        return id;
    }

    private UUID createSessionLog(
            UUID userId,
            UUID planId,
            UUID directionId,
            Timestamp startedAt,
            int durationMinutes
    ) {
        var id = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO session_logs (
                    id,
                    user_id,
                    plan_id,
                    direction_id,
                    started_at,
                    finished_at,
                    duration_minutes,
                    result,
                    notes,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                """,
                id,
                userId,
                planId,
                directionId,
                startedAt,
                Timestamp.from(startedAt.toInstant().plusSeconds(durationMinutes * 60L)),
                durationMinutes,
                "Sessão concluída",
                "Notas de teste"
        );

        return id;
    }
}
