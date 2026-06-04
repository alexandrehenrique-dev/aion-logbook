package br.com.byop.aionlogbook.onboarding.api;

import br.com.byop.aionlogbook.onboarding.application.OnboardingService;
import br.com.byop.aionlogbook.onboarding.dto.CompleteOnboardingResponse;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsRequest;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsResponse;
import br.com.byop.aionlogbook.onboarding.dto.OnboardingStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingService service;

    public OnboardingController(OnboardingService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public OnboardingStatusResponse status() {
        return service.getStatus();
    }

    @PostMapping("/complete")
    public CompleteOnboardingResponse complete() {
        return service.complete();
    }

    @PostMapping("/directions")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOnboardingDirectionsResponse createDirections(
            @Valid @RequestBody CreateOnboardingDirectionsRequest request
    ) {
        return service.createDirections(request);
    }
}