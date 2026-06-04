package br.com.byop.aionlogbook.bugreport.infrastructure;

import br.com.byop.aionlogbook.bugreport.application.BugReportNotifier;
import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TelegramBugReportNotifier implements BugReportNotifier {

    private final TelegramBugReportProperties properties;
    private final RestClient telegramRestClient;

    @Override
    public boolean isEnabled() {
        return properties.enabled();
    }

    @Override
    public void notify(BugReport bugReport) {
        if (!properties.enabled()) {
            return;
        }

        validateConfiguration();

        telegramRestClient
                .post()
                .uri("/bot{token}/sendMessage", properties.botToken())
                .body(requestBody(bugReport))
                .retrieve()
                .toBodilessEntity();
    }

    private void validateConfiguration() {
        if (isBlank(properties.botToken())) {
            throw new IllegalStateException("Telegram bot token is not configured");
        }

        if (isBlank(properties.chatId())) {
            throw new IllegalStateException("Telegram chat id is not configured");
        }
    }

    private Map<String, Object> requestBody(BugReport bugReport) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("chat_id", properties.chatId());
        body.put("text", message(bugReport));
        body.put("parse_mode", "HTML");
        body.put("disable_web_page_preview", true);

        return body;
    }

    private String message(BugReport bugReport) {
        return String.format(
                """
                        <b>Novo Bug Report</b>

                        <b>ID:</b> %s
                        <b>User:</b> %s
                        <b>Severidade:</b> %s
                        <b>Pagina:</b> %s

                        <b>Titulo:</b>
                        %s

                        <b>Descricao:</b>
                        %s
                        """,
                escapeHtml(String.valueOf(bugReport.getId())),
                escapeHtml(String.valueOf(bugReport.getUserId())),
                escapeHtml(String.valueOf(bugReport.getSeverity())),
                escapeHtml(nullToDash(bugReport.getPage())),
                escapeHtml(bugReport.getTitle()),
                escapeHtml(bugReport.getDescription())
        );
    }

    private String nullToDash(String value) {
        return isBlank(value) ? "-" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
