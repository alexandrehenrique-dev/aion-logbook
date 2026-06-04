package br.com.byop.aionlogbook.bugreport.application;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import br.com.byop.aionlogbook.bugreport.dto.BugReportResponse;
import br.com.byop.aionlogbook.bugreport.dto.CreateBugReportRequest;
import br.com.byop.aionlogbook.bugreport.infrastructure.BugReportRepository;
import br.com.byop.aionlogbook.bugreport.mapper.BugReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateBugReportUseCase {

    private static final String REDACTED = "[REDACTED]";

    private final BugReportRepository bugReportRepository;
    private final BugReportNotifier bugReportNotifier;

    public BugReportResponse execute(UUID userId, CreateBugReportRequest request) {
        BugReport bugReport = BugReportMapper.toDomain(userId, request);

        BugReport savedBugReport = bugReportRepository.save(bugReport);

        if (!bugReportNotifier.isEnabled()) {
            return BugReportMapper.toResponse(savedBugReport);
        }

        try {
            bugReportNotifier.notify(savedBugReport);

            BugReport notifiedBugReport = savedBugReport.markTelegramSent();
            BugReport updatedBugReport = bugReportRepository.save(notifiedBugReport);

            return BugReportMapper.toResponse(updatedBugReport);
        } catch (Exception exception) {
            log.warn(
                    "Falha ao enviar bug report para Telegram. bugReportId={}, userId={}, errorType={}",
                    savedBugReport.getId(),
                    savedBugReport.getUserId(),
                    exception.getClass().getSimpleName()
            );

            BugReport failedBugReport = savedBugReport.markTelegramFailed(safeErrorMessage(exception));
            BugReport updatedBugReport = bugReportRepository.save(failedBugReport);

            return BugReportMapper.toResponse(updatedBugReport);
        }
    }

    private String safeErrorMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        String sanitized = redactSensitiveValues(message);

        return sanitized.length() > 1000
                ? sanitized.substring(0, 1000)
                : sanitized;
    }

    private String redactSensitiveValues(String message) {
        return message
                .replaceAll("(?i)/bot[^/\\s]+/sendMessage", "/bot" + REDACTED + "/sendMessage")
                .replaceAll("\\b\\d{5,}:[A-Za-z0-9_-]{20,}\\b", REDACTED)
                .replaceAll("(?i)(authorization|password|secret|token)=([^\\s&]+)", "$1=" + REDACTED)
                .replaceAll("(?i)(authorization|password|secret|token):\\s*([^\\s,;]+)", "$1: " + REDACTED);
    }
}
