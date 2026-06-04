package br.com.byop.aionlogbook.bugreport.mapper;

import br.com.byop.aionlogbook.bugreport.domain.BugReport;
import br.com.byop.aionlogbook.bugreport.dto.BugReportResponse;
import br.com.byop.aionlogbook.bugreport.dto.CreateBugReportRequest;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BugReportMapper {

    private static final String SUCCESS_MESSAGE = "Bug report registrado com sucesso.";

    private static final Set<String> BLOCKED_METADATA_KEYS = Set.of(
            "token",
            "access_token",
            "accesstoken",
            "refresh_token",
            "refreshtoken",
            "id_token",
            "idtoken",
            "authorization",
            "cookie",
            "cookies",
            "localstorage",
            "local_storage",
            "sessionstorage",
            "session_storage",
            "password",
            "secret",
            "client_secret",
            "clientsecret"
    );

    private BugReportMapper() {
    }

    public static BugReport toDomain(UUID userId, CreateBugReportRequest request) {
        return BugReport.received(
                userId,
                request.title(),
                request.description(),
                request.severity(),
                request.page(),
                sanitizeMetadata(request.metadata())
        );
    }

    public static BugReportResponse toResponse(BugReport bugReport) {
        return new BugReportResponse(
                bugReport.getId(),
                bugReport.getStatus(),
                bugReport.isTelegramSent(),
                SUCCESS_MESSAGE
        );
    }

    public static Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> sanitized = new LinkedHashMap<>();

        metadata.forEach((key, value) -> {
            if (key == null || isBlockedKey(key)) {
                return;
            }

            sanitized.put(key, sanitizeValue(value));
        });

        return sanitized;
    }

    @SuppressWarnings("unchecked")
    private static Object sanitizeValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();

            map.forEach((nestedKey, nestedValue) -> {
                if (nestedKey instanceof String key && !isBlockedKey(key)) {
                    nested.put(key, sanitizeValue(nestedValue));
                }
            });

            return nested;
        }

        return value;
    }

    private static boolean isBlockedKey(String key) {
        String normalized = key
                .replace("-", "_")
                .toLowerCase(Locale.ROOT);

        return BLOCKED_METADATA_KEYS.contains(normalized)
                || normalized.contains("token")
                || normalized.contains("cookie")
                || normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("authorization")
                || normalized.contains("localstorage")
                || normalized.contains("local_storage")
                || normalized.contains("sessionstorage")
                || normalized.contains("session_storage");
    }
}
