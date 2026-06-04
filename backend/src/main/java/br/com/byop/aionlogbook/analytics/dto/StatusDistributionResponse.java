package br.com.byop.aionlogbook.analytics.dto;

public record StatusDistributionResponse(
        String status,
        long total,
        double percentage
) {
}