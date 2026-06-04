package br.com.byop.aionlogbook.bugreport.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TelegramBugReportConfig {

    @Bean
    RestClient telegramRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://api.telegram.org")
                .build();
    }
}