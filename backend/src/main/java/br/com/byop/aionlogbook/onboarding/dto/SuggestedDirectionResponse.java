package br.com.byop.aionlogbook.onboarding.dto;

public record SuggestedDirectionResponse(
        String name,
        String description,
        String color,
        String icon,
        String identityPhrase
) {
}