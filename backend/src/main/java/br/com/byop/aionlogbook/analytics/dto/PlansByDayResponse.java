package br.com.byop.aionlogbook.analytics.dto;

import java.time.LocalDate;

public record PlansByDayResponse(
        String day,
        LocalDate date,
        long planned,
        long executed
) {}