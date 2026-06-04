package br.com.byop.aionlogbook.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserProviderTest {

    private final AuthenticatedUserProvider provider = new AuthenticatedUserProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class GetCurrentUser {

        @Test
        void shouldReturnAuthenticatedUserFromJwt() {
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .subject("sub-123")
                    .claim("email", "loki@byop.com")
                    .claim("preferred_username", "loki")
                    .claim("name", "Loki")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken(jwt, null)
            );

            AuthenticatedUser result = provider.getCurrentUser();

            assertThat(result.keycloakSubject()).isEqualTo("sub-123");
            assertThat(result.email()).isEqualTo("loki@byop.com");
            assertThat(result.username()).isEqualTo("loki");
            assertThat(result.fullName()).isEqualTo("Loki");
        }

        @Test
        void shouldThrowWhenAuthenticationIsMissing() {
            assertThatThrownBy(provider::getCurrentUser)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Usuário autenticado não encontrado.");
        }

        @Test
        void shouldThrowWhenPrincipalIsNotJwt() {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("loki", null)
            );

            assertThatThrownBy(provider::getCurrentUser)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Usuário autenticado não encontrado.");
        }

        @Test
        void shouldThrowWhenJwtHasNoSubClaim() {
            // Simula token sem claim 'sub' — ocorre quando o scope 'basic' está ausente no Keycloak
            Jwt jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("email", "loki@byop.com")
                    .claim("preferred_username", "loki")
                    .claim("name", "Loki")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken(jwt, null)
            );

            assertThatThrownBy(provider::getCurrentUser)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("claim 'sub' ausente");
        }
    }
}