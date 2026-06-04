package br.com.byop.aionlogbook.identity.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";
    private static final String DEFAULT_THEME = "system";
    private static final int DEFAULT_PLAN_DURATION = 60;
    private static final int DEFAULT_NOTIFICATION_LEAD_MINUTES = 10;

    @Id
    private UUID id;

    @Column(name = "keycloak_subject", nullable = false, unique = true)
    private String keycloakSubject;

    private String email;

    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false, length = 80)
    private String timezone = DEFAULT_TIMEZONE;

    @Column(nullable = false, length = 20)
    private String theme = DEFAULT_THEME;

    @Column(name = "default_plan_duration", nullable = false)
    private Integer defaultPlanDuration = DEFAULT_PLAN_DURATION;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    @Column(name = "notification_lead_minutes", nullable = false)
    private Integer notificationLeadMinutes = DEFAULT_NOTIFICATION_LEAD_MINUTES;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;

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
        this.timezone = DEFAULT_TIMEZONE;
        this.theme = DEFAULT_THEME;
        this.defaultPlanDuration = DEFAULT_PLAN_DURATION;
        this.notificationsEnabled = true;
        this.notificationLeadMinutes = DEFAULT_NOTIFICATION_LEAD_MINUTES;
        this.onboardingCompleted = false;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateFromToken(String email, String username, String fullName) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateSettings(
            String timezone,
            String theme,
            Integer defaultPlanDuration,
            Boolean notificationsEnabled,
            Integer notificationLeadMinutes
    ) {
        this.timezone = timezone;
        this.theme = theme;
        this.defaultPlanDuration = defaultPlanDuration;
        this.notificationsEnabled = notificationsEnabled;
        this.notificationLeadMinutes = notificationLeadMinutes;
        this.updatedAt = OffsetDateTime.now();
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
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

    public String getTimezone() {
        return timezone;
    }

    public String getTheme() {
        return theme;
    }

    public Integer getDefaultPlanDuration() {
        return defaultPlanDuration;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public Integer getNotificationLeadMinutes() {
        return notificationLeadMinutes;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
