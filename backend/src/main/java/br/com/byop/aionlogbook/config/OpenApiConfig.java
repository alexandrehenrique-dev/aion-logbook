package br.com.byop.aionlogbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI aionLogbookOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aion Logbook API")
                        .version("v1")
                        .description("Backend API foundation for Aion Logbook."));
    }
}
