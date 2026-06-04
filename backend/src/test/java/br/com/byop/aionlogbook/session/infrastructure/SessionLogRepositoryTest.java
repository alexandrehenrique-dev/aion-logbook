package br.com.byop.aionlogbook.session.infrastructure;

import br.com.byop.aionlogbook.session.domain.SessionLog;
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

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SessionLogRepositoryTest extends PostgresRepositoryTest {

    @Autowired
    private SessionLogRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    class FindByIdAndUserId {

        @Test
        @DisplayName("deve retornar sessao quando pertence ao usuario")
        void shouldReturnSessionWhenOwnedByUser() {

            UUID userId = createUserProfile();

            SessionLog session =
                    repository.save(
                            SessionLog.builder()
                                    .userId(userId)
                                    .startedAt(OffsetDateTime.now())
                                    .durationMinutes(30)
                                    .build()
                    );

            var result =
                    repository.findByIdAndUserId(
                            session.getId(),
                            userId
                    );

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("nao deve retornar sessao de outro usuario")
        void shouldNotReturnSessionFromAnotherUser() {

            UUID ownerId = createUserProfile();

            SessionLog session =
                    repository.save(
                            SessionLog.builder()
                                    .userId(ownerId)
                                    .startedAt(OffsetDateTime.now())
                                    .durationMinutes(30)
                                    .build()
                    );

            var result =
                    repository.findByIdAndUserId(
                            session.getId(),
                            UUID.randomUUID()
                    );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindLastDashboardSessions {

        @Test
        @DisplayName("deve enriquecer sessoes apenas com plano e direcao do mesmo usuario")
        void shouldEnrichSessionsOnlyWithPlanAndDirectionFromSameUser() {
            UUID userId = createUserProfile();
            UUID anotherUserId = createUserProfile();
            UUID directionId = createDirection(userId, "Direcao propria");
            UUID planId = createPlan(userId, directionId, "Plano proprio");
            UUID otherDirectionId = createDirection(anotherUserId, "Direcao de outro usuario");
            UUID otherPlanId = createPlan(anotherUserId, otherDirectionId, "Plano de outro usuario");

            repository.save(
                    SessionLog.builder()
                            .userId(userId)
                            .planId(planId)
                            .directionId(directionId)
                            .startedAt(OffsetDateTime.parse("2026-06-04T10:00:00-03:00"))
                            .durationMinutes(30)
                            .build()
            );

            repository.save(
                    SessionLog.builder()
                            .userId(userId)
                            .planId(otherPlanId)
                            .directionId(otherDirectionId)
                            .startedAt(OffsetDateTime.parse("2026-06-04T11:00:00-03:00"))
                            .durationMinutes(45)
                            .build()
            );

            var result = repository.findLastDashboardSessions(userId, PageRequest.of(0, 5));

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getPlanId()).isEqualTo(otherPlanId);
            assertThat(result.getFirst().getPlanTitle()).isNull();
            assertThat(result.getFirst().getDirectionName()).isNull();
            assertThat(result.get(1).getPlanId()).isEqualTo(planId);
            assertThat(result.get(1).getPlanTitle()).isEqualTo("Plano proprio");
            assertThat(result.get(1).getDirectionName()).isEqualTo("Direcao propria");
        }

        @Test
        @DisplayName("deve respeitar paginacao nas sessoes recentes do dashboard")
        void shouldRespectDashboardSessionsPageable() {
            UUID userId = createUserProfile();

            for (int index = 0; index < 6; index++) {
                repository.save(
                        SessionLog.builder()
                                .userId(userId)
                                .startedAt(OffsetDateTime.parse("2026-06-04T10:00:00-03:00").plusMinutes(index))
                                .durationMinutes(15)
                                .build()
                );
            }

            var result = repository.findLastDashboardSessions(userId, PageRequest.of(0, 5));

            assertThat(result).hasSize(5);
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

    private UUID createDirection(UUID userId, String name) {
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
                name,
                "ACTIVE"
        );

        return id;
    }

    private UUID createPlan(UUID userId, UUID directionId, String title) {
        var id = UUID.randomUUID();

        jdbcTemplate.update("""
                INSERT INTO plans (
                    id,
                    user_id,
                    direction_id,
                    title,
                    priority,
                    status,
                    notify,
                    created_at,
                    updated_at,
                    last_status_changed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, now(), now(), now())
                """,
                id,
                userId,
                directionId,
                title,
                "MEDIUM",
                "PENDING",
                false
        );

        return id;
    }
}
