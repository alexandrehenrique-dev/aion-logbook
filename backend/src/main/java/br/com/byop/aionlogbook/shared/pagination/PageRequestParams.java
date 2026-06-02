package br.com.byop.aionlogbook.shared.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequestParams(
        @Min(0) int page,
        @Min(1) @Max(100) int size
) {
    public PageRequestParams {
        if (size == 0) {
            size = 20;
        }
    }
}
