package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.config.AppCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_STATIC_ROUTES = {
            "/",
            "/index.html",
            "/favicon.ico",
            "/silent-check-sso.html",
            "/assets/**",
            "/brand/**",
            "/*.js",
            "/*.css",
            "/*.ico",
            "/*.png",
            "/*.svg",
            "/*.webp",
            "/*.woff",
            "/*.woff2"
    };

    private static final String[] PUBLIC_ACTUATOR_ROUTES = {
            "/actuator/health",
            "/actuator/info"
    };

    private static final String[] PUBLIC_SWAGGER_ROUTES = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Environment environment,
            SecurityProperties securityProperties
    ) throws Exception {
        configureBaseSecurity(http);

        if (!securityProperties.enabled()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        boolean swaggerEnabled = isSwaggerEnabled(environment);

        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    auth.requestMatchers(PUBLIC_STATIC_ROUTES).permitAll();
                    auth.requestMatchers(PUBLIC_ACTUATOR_ROUTES).permitAll();

                    if (swaggerEnabled) {
                        auth.requestMatchers(PUBLIC_SWAGGER_ROUTES).permitAll();
                    }

                    auth.requestMatchers("/api/v1/**").authenticated();

                    auth.anyRequest().permitAll();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppCorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(corsProperties.allowedOrigins());
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private static void configureBaseSecurity(HttpSecurity http) throws Exception {
        http
                // CSRF não é necessário em APIs stateless com JWT — tokens não são enviados via cookie
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // SAMEORIGIN permite que o silent-check-sso.html do Keycloak.js seja carregado em iframe
                // da mesma origem. DENY bloquearia o fluxo de check-sso e causaria loading infinito.
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );
    }

    private static final Set<String> SWAGGER_PROFILES = Set.of("local", "dev");

    private static boolean isSwaggerEnabled(Environment environment) {
        return !Collections.disjoint(Set.of(environment.getActiveProfiles()), SWAGGER_PROFILES);
    }
}
