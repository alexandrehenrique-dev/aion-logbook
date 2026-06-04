package br.com.byop.aionlogbook.bugreport.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BugReportTest {

    @Nested
    class Received {

        @Test
        void shouldCreateReceivedBugReportWithDefaultValues() {
            UUID userId = UUID.randomUUID();

            BugReport bugReport = BugReport.received(
                    userId,
                    "Erro ao salvar direção",
                    "Ao clicar em salvar direção, nada acontece na tela.",
                    BugReportSeverity.HIGH,
                    "/directions/new",
                    Map.of("browser", "Chrome")
            );

            assertThat(bugReport.getId()).isNotNull();
            assertThat(bugReport.getUserId()).isEqualTo(userId);
            assertThat(bugReport.getTitle()).isEqualTo("Erro ao salvar direção");
            assertThat(bugReport.getDescription()).isEqualTo("Ao clicar em salvar direção, nada acontece na tela.");
            assertThat(bugReport.getSeverity()).isEqualTo(BugReportSeverity.HIGH);
            assertThat(bugReport.getPage()).isEqualTo("/directions/new");
            assertThat(bugReport.getMetadata()).containsEntry("browser", "Chrome");
            assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.RECEIVED);
            assertThat(bugReport.isTelegramSent()).isFalse();
            assertThat(bugReport.getTelegramError()).isNull();
            assertThat(bugReport.getCreatedAt()).isNotNull();
            assertThat(bugReport.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class MarkTelegramSent {

        @Test
        void shouldMarkBugReportAsTelegramSent() {
            BugReport bugReport = BugReport.received(
                    UUID.randomUUID(),
                    "Erro ao abrir modal",
                    "O modal não abre ao clicar no botão de nova direção.",
                    BugReportSeverity.MEDIUM,
                    "/directions",
                    Map.of()
            );

            BugReport updated = bugReport.markTelegramSent();

            assertThat(updated.getId()).isEqualTo(bugReport.getId());
            assertThat(updated.isTelegramSent()).isTrue();
            assertThat(updated.getTelegramError()).isNull();
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(bugReport.getUpdatedAt());
        }
    }

    @Nested
    class MarkTelegramFailed {

        @Test
        void shouldMarkBugReportAsTelegramFailedWithSafeError() {
            BugReport bugReport = BugReport.received(
                    UUID.randomUUID(),
                    "Erro crítico no logbook",
                    "A tela quebra ao tentar registrar uma entrada com metadata.",
                    BugReportSeverity.CRITICAL,
                    "/logbook",
                    Map.of()
            );

            BugReport updated = bugReport.markTelegramFailed("Telegram request failed with status 500");

            assertThat(updated.getId()).isEqualTo(bugReport.getId());
            assertThat(updated.isTelegramSent()).isFalse();
            assertThat(updated.getTelegramError()).isEqualTo("Telegram request failed with status 500");
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(bugReport.getUpdatedAt());
        }
    }
}
