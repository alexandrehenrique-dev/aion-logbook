package br.com.byop.aionlogbook.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatedUserProvider.class);

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Usuário autenticado não encontrado.");
        }

        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            log.error(
                "JWT recebido sem claim 'sub'. Claims disponíveis: {}. " +
                "Causa provável: o scope 'basic' não está nos defaultClientScopes do client Keycloak. " +
                "Solução: adicionar o scope 'basic' ao client 'aion-logbook-web' no Keycloak Admin Console " +
                "(Clients → aion-logbook-web → Client Scopes → Add client scope → basic → Default).",
                jwt.getClaims().keySet()
            );
            throw new IllegalStateException(
                "JWT inválido: claim 'sub' ausente. " +
                "Adicione o scope 'basic' aos defaultClientScopes do client no Keycloak."
            );
        }

        return new AuthenticatedUser(
                subject,
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("name")
        );
    }
}
