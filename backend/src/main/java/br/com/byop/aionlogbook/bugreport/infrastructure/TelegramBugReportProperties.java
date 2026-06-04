package br.com.byop.aionlogbook.bugreport.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.telegram.bug-report")
public record TelegramBugReportProperties(
        boolean enabled,
        String botToken,
        String chatId
) {
}