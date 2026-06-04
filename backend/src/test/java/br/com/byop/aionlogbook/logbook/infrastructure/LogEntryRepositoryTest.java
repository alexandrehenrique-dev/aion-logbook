package br.com.byop.aionlogbook.logbook.infrastructure;

import br.com.byop.aionlogbook.logbook.application.LogEntrySearchCriteria;
import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class LogEntryRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Autowired
    private LogEntryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindByIdAndUserIdWhenOwner() {
        var userId = createUserProfile();
        var entry = logEntry(userId, LogEntryType.REFLECTION);

        entityManager.persistAndFlush(entry);

        var result = repository.findByIdAndUserId(entry.getId(), userId);

        assertThat(result).isPresent();
    }

    @Test
    void shouldNotFindByIdAndUserIdWhenAnotherUser() {
        UUID userId = createUserProfile();
        var entry = logEntry(userId, LogEntryType.REFLECTION);

        entityManager.persistAndFlush(entry);

        var result = repository.findByIdAndUserId(entry.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFilterByType() {
        var userId = createUserProfile();

        entityManager.persist(logEntry(userId, LogEntryType.REFLECTION));
        entityManager.persist(logEntry(userId, LogEntryType.IDEA));
        entityManager.flush();

        var criteria = new LogEntrySearchCriteria(LogEntryType.IDEA, null, null, null, null, null, null);
        var result = repository.findByFilters(userId, criteria, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(1)
                .allMatch(entry -> entry.getType() == LogEntryType.IDEA);
    }

    @Test
    void shouldFilterByQInTitleOrContent() {
        var userId = createUserProfile();

        var entry = logEntry(userId, LogEntryType.LEARNING);
        entry.setTitle("A dor virou código");

        entityManager.persist(entry);
        entityManager.persist(logEntry(userId, LogEntryType.LEARNING));
        entityManager.flush();

        var criteria = new LogEntrySearchCriteria(null, null, null, null, null, null, "dor");
        var result = repository.findByFilters(userId, criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).contains("dor");
    }

    private LogEntry logEntry(UUID userId, LogEntryType type) {
        var entry = new LogEntry();

        entry.setId(UUID.randomUUID());
        entry.setUserId(userId);
        entry.setTitle("Título");
        entry.setContent("Conteúdo seguro");
        entry.setType(type);
        entry.setTags(List.of("aion"));
        entry.setCreatedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());

        return entry;
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
