package br.com.byop.aionlogbook.bugreport.infrastructure;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import br.com.byop.aionlogbook.bugreport.domain.BugReportSeverity;
import br.com.byop.aionlogbook.bugreport.domain.BugReportStatus;
import br.com.byop.aionlogbook.support.PostgresRepositoryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BugReportRepositoryTest extends PostgresRepositoryTest {

    @Autowired
    private BugReportRepository repository;

    @Nested
    class Save {

        @Test
        void shouldSaveBugReport() {
            var bugReport = BugReport.received(
                    UUID.randomUUID(),
                    "Erro ao abrir modal",
                    "Ao clicar no botão de nova direção, o modal não abre.",
                    BugReportSeverity.HIGH,
                    "/directions",
                    Map.of("browser", "Chrome")
            );

            var saved = repository.save(bugReport);

            assertThat(saved.getId()).isEqualTo(bugReport.getId());
            assertThat(saved.getStatus()).isEqualTo(BugReportStatus.RECEIVED);
            assertThat(saved.isTelegramSent()).isFalse();
        }
    }
}
