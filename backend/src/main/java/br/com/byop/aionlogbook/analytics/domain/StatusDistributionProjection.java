package br.com.byop.aionlogbook.analytics.domain;

public record StatusDistributionProjection(
        String status,
        long count
) {
}