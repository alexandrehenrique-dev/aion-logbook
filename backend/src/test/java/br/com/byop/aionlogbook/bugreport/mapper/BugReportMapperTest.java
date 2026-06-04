package br.com.byop.aionlogbook.bugreport.mapper;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import br.com.byop.aionlogbook.bugreport.domain.BugReportSeverity;
import br.com.byop.aionlogbook.bugreport.domain.BugReportStatus;
import br.com.byop.aionlogbook.bugreport.dto.CreateBugReportRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BugReportMapperTest {

    @Nested
    class ToDomain {

        @Test
        void shouldCreateDomainWithAuthenticatedUserId() {
            UUID userId = UUID.randomUUID();

            CreateBugReportRequest request = new CreateBugReportRequest(
                    "Erro no botão salvar",
                    "Ao clicar no botão salvar, nada acontece na interface.",
                    BugReportSeverity.HIGH,
                    "/directions",
                    Map.of("browser", "Chrome")
            );

            BugReport bugReport = BugReportMapper.toDomain(userId, request);

            assertThat(bugReport).satisfies(b -> {
                assertThat(b.getUserId()).isEqualTo(userId);
                assertThat(b.getTitle()).isEqualTo(request.title());
                assertThat(b.getDescription()).isEqualTo(request.description());
                assertThat(b.getSeverity()).isEqualTo(BugReportSeverity.HIGH);
                assertThat(b.getPage()).isEqualTo("/directions");
                assertThat(b.getStatus()).isEqualTo(BugReportStatus.RECEIVED);
                assertThat(b.isTelegramSent()).isFalse();
            });
        }

        @Test
        void shouldSanitizeSensitiveMetadata() {
            UUID userId = UUID.randomUUID();

            CreateBugReportRequest request = new CreateBugReportRequest(
                    "Erro com metadata",
                    "Erro ao enviar bug report contendo metadata sensível.",
                    BugReportSeverity.CRITICAL,
                    "/logbook",
                    Map.of(
                            "browser", "Chrome",
                            "accessToken", "secret-token",
                            "authToken", "auth-token",
                            "cookies", "session-cookie",
                            "localStorage", "sensitive-local-storage"
                    )
            );

            BugReport bugReport = BugReportMapper.toDomain(userId, request);

            assertThat(bugReport.getMetadata())
                    .containsEntry("browser", "Chrome")
                    .doesNotContainKeys("accessToken", "authToken", "cookies", "localStorage");
        }

        @Test
        void shouldSanitizeNestedSensitiveMetadata() {
            Map<String, Object> sanitized = BugReportMapper.sanitizeMetadata(
                    Map.of(
                            "browser", "Chrome",
                            "context", Map.of(
                                    "page", "/directions",
                                    "authorization", "Bearer token"
                            )
                    )
            );

            assertThat(sanitized).containsKey("context");

            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) sanitized.get("context");

            assertThat(context)
                    .containsEntry("page", "/directions")
                    .doesNotContainKey("authorization");
        }
    }

    @Nested
    class ToResponse {

        @Test
        void shouldCreateSuccessResponse() {
            BugReport bugReport = BugReport.received(
                    UUID.randomUUID(),
                    "Erro ao abrir modal",
                    "Ao clicar no botão de nova direção, o modal não abre.",
                    BugReportSeverity.MEDIUM,
                    "/directions",
                    Map.of()
            );

            var response = BugReportMapper.toResponse(bugReport);

            assertThat(response).satisfies(r -> {
                assertThat(r.id()).isEqualTo(bugReport.getId());
                assertThat(r.status()).isEqualTo(BugReportStatus.RECEIVED);
                assertThat(r.telegramSent()).isFalse();
                assertThat(r.message()).isEqualTo("Bug report registrado com sucesso.");
            });
        }
    }
}
