package br.com.byop.aionlogbook.shared.web;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        OffsetDateTime timestamp,
        T data
) {
}
