package br.com.byop.aionlogbook.direction.domain;

import br.com.byop.aionlogbook.identity.domain.UserProfile;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "directions")
public class Direction {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 20)
    private String color;

    @Column(length = 100)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DirectionStatus status;

    @Column(name = "identity_phrase", length = 200)
    private String identityPhrase;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Direction() {
    }

    public Direction(
            UserProfile userProfile,
            String name,
            String description,
            String color,
            String icon,
            String identityPhrase,
            OffsetDateTime now
    ) {
        this.id = UUID.randomUUID();
        this.userProfile = userProfile;
        this.name = name;
        this.description = description;
        this.color = color;
        this.icon = icon;
        this.identityPhrase = identityPhrase;
        this.status = DirectionStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            String name,
            String description,
            String color,
            String icon,
            String identityPhrase,
            OffsetDateTime now
    ) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (color != null) this.color = color;
        if (icon != null) this.icon = icon;
        if (identityPhrase != null) this.identityPhrase = identityPhrase;
        this.updatedAt = now;
    }

    public void archive(OffsetDateTime now) {
        this.status = DirectionStatus.ARCHIVED;
        this.archivedAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UserProfile getUserProfile() { return userProfile; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
    public DirectionStatus getStatus() { return status; }
    public String getIdentityPhrase() { return identityPhrase; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
