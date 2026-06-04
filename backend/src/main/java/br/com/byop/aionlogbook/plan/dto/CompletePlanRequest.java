package br.com.byop.aionlogbook.plan.dto;

public record CompletePlanRequest(
        Integer actualMinutes,
        String description
) {}
