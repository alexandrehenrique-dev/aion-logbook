package br.com.byop.aionlogbook.bugreport.dto;

import br.com.byop.aionlogbook.bugreport.domain.BugReportStatus;

import java.util.UUID;

public record BugReportResponse(
        UUID id,
        BugReportStatus status,
        boolean telegramSent,
        String message
) {
}