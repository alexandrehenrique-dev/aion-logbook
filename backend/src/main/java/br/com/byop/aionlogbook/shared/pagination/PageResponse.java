package br.com.byop.aionlogbook.shared.pagination;

import java.util.List;

public record PageResponse<T>(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<T> content
) {
}
