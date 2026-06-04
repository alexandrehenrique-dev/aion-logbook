package br.com.byop.aionlogbook.onboarding.dto;

import java.util.List;

public record OnboardingStatusResponse(
        Boolean completed,
        List<SuggestedDirectionResponse> suggestedDirections
) {
}