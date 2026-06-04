package br.com.byop.aionlogbook.analytics.domain;

import java.time.LocalDate;

public record PlansByDayProjection(
        LocalDate date,
        long planned,
        long executed
) {
}