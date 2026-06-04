package br.com.byop.aionlogbook.bugreport.application;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import br.com.byop.aionlogbook.bugreport.domain.BugReportSeverity;
import br.com.byop.aionlogbook.bugreport.domain.BugReportStatus;
import br.com.byop.aionlogbook.bugreport.dto.CreateBugReportRequest;
import br.com.byop.aionlogbook.bugreport.infrastructure.BugReportRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBugReportUseCaseTest {

    @Mock
    private BugReportRepository bugReportRepository;

    @Mock
    private BugReportNotifier bugReportNotifier;

    @InjectMocks
    private CreateBugReportUseCase useCase;

    @Nested
    class Execute {

        @Test
        void shouldPersistBugReportBeforeTelegramNotification() {
            UUID userId = UUID.randomUUID();

            CreateBugReportRequest request = validRequest();

            when(bugReportRepository.save(any(BugReport.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(bugReportNotifier.isEnabled()).thenReturn(true);

            var response = useCase.execute(userId, request);

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);

            verify(bugReportRepository, times(2)).save(captor.capture());
            verify(bugReportNotifier).notify(any(BugReport.class));

            BugReport firstSave = captor.getAllValues().get(0);
            BugReport secondSave = captor.getAllValues().get(1);

            assertThat(firstSave.isTelegramSent()).isFalse();
            assertThat(secondSave.isTelegramSent()).isTrue();

            assertThat(response.id()).isEqualTo(secondSave.getId());
            assertThat(response.status()).isEqualTo(BugReportStatus.RECEIVED);
            assertThat(response.telegramSent()).isTrue();
            assertThat(response.message()).isEqualTo("Bug report registrado com sucesso.");
        }

        @Test
        void shouldNotNotifyTelegramWhenDisabled() {
            UUID userId = UUID.randomUUID();

            when(bugReportRepository.save(any(BugReport.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(bugReportNotifier.isEnabled()).thenReturn(false);

            var response = useCase.execute(userId, validRequest());

            verify(bugReportRepository, times(1)).save(any(BugReport.class));
            verify(bugReportNotifier, never()).notify(any(BugReport.class));

            assertThat(response.telegramSent()).isFalse();
            assertThat(response.status()).isEqualTo(BugReportStatus.RECEIVED);
        }

        @Test
        void shouldReturnSuccessWhenTelegramFails() {
            UUID userId = UUID.randomUUID();

            when(bugReportRepository.save(any(BugReport.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(bugReportNotifier.isEnabled()).thenReturn(true);

            doThrow(new RuntimeException("Telegram request failed with status 500"))
                    .when(bugReportNotifier)
                    .notify(any(BugReport.class));

            var response = useCase.execute(userId, validRequest());

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);

            verify(bugReportRepository, times(2)).save(captor.capture());

            BugReport failedBugReport = captor.getAllValues().get(1);

            assertThat(failedBugReport.isTelegramSent()).isFalse();
            assertThat(failedBugReport.getTelegramError())
                    .isEqualTo("Telegram request failed with status 500");

            assertThat(response.status()).isEqualTo(BugReportStatus.RECEIVED);
            assertThat(response.telegramSent()).isFalse();
            assertThat(response.message()).isEqualTo("Bug report registrado com sucesso.");
        }

        @Test
        void shouldRedactSensitiveDataFromTelegramFailureMessage() {
            UUID userId = UUID.randomUUID();
            String botToken = "123456789:" + "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef";

            when(bugReportRepository.save(any(BugReport.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(bugReportNotifier.isEnabled()).thenReturn(true);

            doThrow(new RuntimeException(
                    "POST https://api.telegram.org/bot" + botToken
                            + "/sendMessage?token=" + botToken
                            + " authorization: BearerSecret"
            ))
                    .when(bugReportNotifier)
                    .notify(any(BugReport.class));

            useCase.execute(userId, validRequest());

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository, times(2)).save(captor.capture());

            BugReport failedBugReport = captor.getAllValues().get(1);

            assertThat(failedBugReport.getTelegramError()).doesNotContain(botToken);
            assertThat(failedBugReport.getTelegramError()).contains("[REDACTED]");
        }

        @Test
        void shouldSanitizeMetadataBeforePersisting() {
            UUID userId = UUID.randomUUID();

            CreateBugReportRequest request = new CreateBugReportRequest(
                    "Erro com token",
                    "Erro ao enviar metadata com dados sensíveis.",
                    BugReportSeverity.CRITICAL,
                    "/logbook",
                    Map.of(
                            "browser", "Chrome",
                            "token", "secret",
                            "authorization", "Bearer secret"
                    )
            );

            when(bugReportRepository.save(any(BugReport.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(bugReportNotifier.isEnabled()).thenReturn(false);

            useCase.execute(userId, request);

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());

            BugReport saved = captor.getValue();

            assertThat(saved.getMetadata()).containsEntry("browser", "Chrome");
            assertThat(saved.getMetadata()).doesNotContainKeys("token", "authorization");
        }
    }

    private CreateBugReportRequest validRequest() {
        return new CreateBugReportRequest(
                "Erro ao salvar direção",
                "Ao clicar no botão salvar direção, nada acontece na tela.",
                BugReportSeverity.HIGH,
                "/directions/new",
                Map.of("browser", "Chrome")
        );
    }
}
