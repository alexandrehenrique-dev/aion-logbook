package br.com.byop.aionlogbook.bugreport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bug_reports")
public class BugReport {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BugReportSeverity severity;

    @Column(length = 500)
    private String page;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BugReportStatus status;

    @Column(name = "telegram_sent", nullable = false)
    private boolean telegramSent;

    @Column(name = "telegram_error", length = 1000)
    private String telegramError;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static BugReport received(
            UUID userId,
            String title,
            String description,
            BugReportSeverity severity,
            String page,
            Map<String, Object> metadata
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        return new BugReport(
                UUID.randomUUID(),
                userId,
                title,
                description,
                severity,
                page,
                metadata,
                BugReportStatus.RECEIVED,
                false,
                null,
                now,
                now
        );
    }

    public BugReport markTelegramSent() {
        return new BugReport(
                id,
                userId,
                title,
                description,
                severity,
                page,
                metadata,
                status,
                true,
                null,
                createdAt,
                OffsetDateTime.now()
        );
    }

    public BugReport markTelegramFailed(String safeErrorMessage) {
        return new BugReport(
                id,
                userId,
                title,
                description,
                severity,
                page,
                metadata,
                status,
                false,
                safeErrorMessage,
                createdAt,
                OffsetDateTime.now()
        );
    }
}
