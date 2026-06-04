package br.com.byop.aionlogbook.plan.dto;

public record PartialPlanRequest(
        Integer actualMinutes,
        String reason,
        String description
) {}
