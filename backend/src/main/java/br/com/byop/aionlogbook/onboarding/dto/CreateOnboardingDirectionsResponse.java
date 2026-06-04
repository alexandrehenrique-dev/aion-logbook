package br.com.byop.aionlogbook.onboarding.dto;

import br.com.byop.aionlogbook.direction.dto.DirectionResponse;

import java.util.List;

public record CreateOnboardingDirectionsResponse(
        Integer created,
        List<DirectionResponse> directions
) {
}