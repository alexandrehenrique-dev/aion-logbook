package br.com.byop.aionlogbook.bugreport.dto;

import br.com.byop.aionlogbook.bugreport.domain.BugReportSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateBugReportRequest(
        @NotBlank
        @Size(min = 5, max = 200)
        String title,

        @NotBlank
        @Size(min = 10, max = 2000)
        String description,

        @NotNull
        BugReportSeverity severity,

        @Size(max = 500)
        String page,

        Map<String, Object> metadata
) {
}