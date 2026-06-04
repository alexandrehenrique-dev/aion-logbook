package br.com.byop.aionlogbook.session.infrastructure;

import br.com.byop.aionlogbook.session.domain.SessionLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SessionLogRepositoryTest {

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
}