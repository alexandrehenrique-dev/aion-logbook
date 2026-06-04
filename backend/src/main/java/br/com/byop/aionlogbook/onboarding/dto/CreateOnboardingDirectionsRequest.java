package br.com.byop.aionlogbook.onboarding.dto;

import br.com.byop.aionlogbook.direction.dto.CreateDirectionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOnboardingDirectionsRequest(
        @NotEmpty
        List<@Valid CreateDirectionRequest> directions
) {
}