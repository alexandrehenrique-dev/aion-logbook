package br.com.byop.aionlogbook.bugreport.infrastructure;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import br.com.byop.aionlogbook.bugreport.domain.BugReportSeverity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TelegramBugReportNotifierTest {

    private final RestClient restClient = mock(RestClient.class);
    private final RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    private final RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
    private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    @Nested
    class IsEnabled {

        @Test
        void shouldReturnFalseWhenTelegramIsDisabled() {
            TelegramBugReportNotifier notifier = new TelegramBugReportNotifier(
                    new TelegramBugReportProperties(false, "", ""),
                    restClient
            );

            assertThatCode(() -> notifier.notify(validBugReport()))
                    .doesNotThrowAnyException();

            verifyNoInteractions(restClient);
        }
    }

    @Nested
    class Notify {

        @Test
        void shouldSendMessageWhenEnabled() {
            TelegramBugReportNotifier notifier = new TelegramBugReportNotifier(
                    new TelegramBugReportProperties(true, "123:abc", "-100123456789"),
                    restClient
            );

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("/bot{token}/sendMessage", "123:abc"))
                    .thenReturn(requestBodySpec);
            when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toBodilessEntity()).thenReturn(null);

            notifier.notify(validBugReport());

            verify(restClient).post();
            verify(requestBodyUriSpec).uri("/bot{token}/sendMessage", "123:abc");
            verify(requestBodySpec).body(any(Map.class));
            verify(requestBodySpec).retrieve();
        }

        @Test
        void shouldThrowWhenEnabledAndBotTokenIsMissing() {
            TelegramBugReportNotifier notifier = new TelegramBugReportNotifier(
                    new TelegramBugReportProperties(true, "", "-100123456789"),
                    restClient
            );

            assertThatThrownBy(() -> notifier.notify(validBugReport()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Telegram bot token is not configured");

            verifyNoInteractions(restClient);
        }

        @Test
        void shouldThrowWhenEnabledAndChatIdIsMissing() {
            TelegramBugReportNotifier notifier = new TelegramBugReportNotifier(
                    new TelegramBugReportProperties(true, "123:abc", ""),
                    restClient
            );

            assertThatThrownBy(() -> notifier.notify(validBugReport()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Telegram chat id is not configured");

            verifyNoInteractions(restClient);
        }
    }

    private BugReport validBugReport() {
        return BugReport.received(
                UUID.randomUUID(),
                "Erro ao salvar direção",
                "Ao clicar no botão salvar direção, nada acontece na tela.",
                BugReportSeverity.HIGH,
                "/directions/new",
                Map.of("browser", "Chrome")
        );
    }
}