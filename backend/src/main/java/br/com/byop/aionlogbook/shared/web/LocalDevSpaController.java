package br.com.byop.aionlogbook.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Em profile local, o frontend roda no servidor de desenvolvimento Vite (porta 5173).
 * O backend Spring Boot é apenas API — não serve HTML de aplicação.
 *
 * Quando alguém acessa localhost:8080 por engano (ou quando o Keycloak redireciona
 * para o backend), esta classe retorna uma página HTML mínima com redirecionamento
 * JavaScript para o Vite. O redirect JS preserva path, query e fragment (incluindo
 * o código de autorização do Keycloak que vem no fragment/query da URL).
 */
@Profile("local")
@Controller
public class LocalDevSpaController {

    @Value("${app.frontend.dev-server:http://localhost:5173}")
    private String devServerUrl;

    @GetMapping(
        value = {"/", "/login", "/dashboard", "/onboarding",
                 "/directions/**", "/plans/**", "/sessions/**",
                 "/calendar/**", "/analytics/**", "/settings/**",
                 "/journal", "/showcase"},
        produces = MediaType.TEXT_HTML_VALUE
    )
    @ResponseBody
    public String redirectToDevServer(HttpServletRequest request) {
        String base = devServerUrl.replaceAll("/$", "");
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <title>Aion Logbook — Dev</title>
                  <script>
                    // Preserva path, query e fragment (auth code do Keycloak fica no query/fragment).
                    window.location.replace('%s' + window.location.pathname + window.location.search + window.location.hash);
                  </script>
                </head>
                <body></body>
                </html>
                """.formatted(base);
    }
}
