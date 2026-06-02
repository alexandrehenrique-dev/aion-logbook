package br.com.byop.aionlogbook.identity.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private UUID id;

    @Column(name = "keycloak_subject", nullable = false, unique = true)
    private String keycloakSubject;

    private String email;

    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserProfile() {
    }

    public UserProfile(String keycloakSubject, String email, String username, String fullName) {
        this.id = UUID.randomUUID();
        this.keycloakSubject = keycloakSubject;
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateFromToken(String email, String username, String fullName) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakSubject() {
        return keycloakSubject;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
