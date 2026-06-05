package br.com.byop.aionlogbook.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve as rotas SPA ou, quando não há index.html no classpath,
 * redireciona via JS para o servidor de desenvolvimento Vite.
 *
 * - index.html presente (prod, full-stack local): forward para index.html.
 * - index.html ausente (backend somente API + Vite externo): JS redirect para
 *   app.frontend.dev-server, preservando path/query/hash para que o auth
 *   code do Keycloak chegue intacto ao Vite.
 */
@Controller
public class SpaController {

    private final ResourceLoader resourceLoader;
    private final String devServerUrl;

    // Cache: evita re-checar o classpath a cada request.
    private Boolean indexHtmlExists;

    public SpaController(
            ResourceLoader resourceLoader,
            @Value("${app.frontend.dev-server:http://localhost:5173}") String devServerUrl
    ) {
        this.resourceLoader = resourceLoader;
        this.devServerUrl = devServerUrl.replaceAll("/$", "");
    }

    @GetMapping(value = {
            "/",
            "/login",
            "/dashboard",
            "/onboarding",
            "/directions/**",
            "/plans/**",
            "/sessions/**",
            "/calendar/**",
            "/analytics/**",
            "/settings/**",
            "/journal",
            "/showcase"
    })
    public void handleSpaRoute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (hasIndexHtml()) {
            request.getRequestDispatcher("/index.html").forward(request, response);
        } else {
            serveDevRedirect(response);
        }
    }

    private boolean hasIndexHtml() {
        if (indexHtmlExists == null) {
            Resource resource = resourceLoader.getResource("classpath:static/index.html");
            indexHtmlExists = resource.exists();
        }
        return indexHtmlExists;
    }

    private void serveDevRedirect(HttpServletResponse response) throws Exception {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(
                """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <title>Aion Logbook</title>
                  <script>
                    window.location.replace('%s' + window.location.pathname + window.location.search + window.location.hash);
                  </script>
                </head>
                <body></body>
                </html>
                """.formatted(devServerUrl)
        );
    }
}
