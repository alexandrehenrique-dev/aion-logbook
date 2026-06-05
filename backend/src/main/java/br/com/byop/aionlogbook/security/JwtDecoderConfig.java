package br.com.byop.aionlogbook.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configura um JwtDecoder que separa o acesso interno ao JWK Set URI
 * (Docker-internal) da validação do issuer (URL externa do Keycloak).
 *
 * Problema que esta classe resolve:
 * KC_HOSTNAME define o issuer nos tokens como a URL pública (ex: https://192.168.0.77/realms/aion-logbook).
 * Mas o backend, dentro do Docker, só alcança o Keycloak pelo nome de serviço interno
 * (http://keycloak:8080). Usar issuer-uri diretamente causaria um mismatch entre a URL
 * configurada e o issuer retornado pelo discovery document, resultando em IllegalStateException
 * não capturada pelo BearerTokenAuthenticationFilter → HTTP 500 em todos os endpoints.
 *
 * Solução:
 * - jwkSetUri aponta para a URL interna Docker (para buscar as chaves)
 * - issuerUri aponta para a URL externa (para validar o claim iss do token)
 *
 * Para desenvolvimento local, keycloak.jwk-set-uri assume o default
 * ${keycloak.issuer-uri}/protocol/openid-connect/certs, que é o mesmo host,
 * então o comportamento é idêntico ao anterior.
 */
@Configuration
public class JwtDecoderConfig {

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${keycloak.jwk-set-uri}") String jwkSetUri,
            @Value("${keycloak.issuer-uri}") String issuerUri
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }
}
