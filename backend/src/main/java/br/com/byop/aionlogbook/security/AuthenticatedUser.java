package br.com.byop.aionlogbook.security;

public record AuthenticatedUser(
        String keycloakSubject,
        String email,
        String username,
        String fullName
) {
}
