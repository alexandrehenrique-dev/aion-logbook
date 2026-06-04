package br.com.byop.aionlogbook.onboarding.api;

import br.com.byop.aionlogbook.onboarding.application.OnboardingService;
import br.com.byop.aionlogbook.onboarding.dto.CompleteOnboardingResponse;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsRequest;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsResponse;
import br.com.byop.aionlogbook.onboarding.dto.OnboardingStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingControllerTest {

    @Mock
    private OnboardingService service;

    @InjectMocks
    private OnboardingController controller;

    @Test
    void shouldReturnStatus() {
        OnboardingStatusResponse response =
                new OnboardingStatusResponse(false, List.of());

        when(service.getStatus()).thenReturn(response);

        assertThat(controller.status()).isEqualTo(response);
    }

    @Test
    void shouldCompleteOnboarding() {
        CompleteOnboardingResponse response =
                new CompleteOnboardingResponse(true);

        when(service.complete()).thenReturn(response);

        assertThat(controller.complete()).isEqualTo(response);
    }

    @Test
    void shouldCreateDirections() {
        CreateOnboardingDirectionsRequest request =
                new CreateOnboardingDirectionsRequest(List.of());

        CreateOnboardingDirectionsResponse response =
                new CreateOnboardingDirectionsResponse(0, List.of());

        when(service.createDirections(request)).thenReturn(response);

        assertThat(controller.createDirections(request))
                .isEqualTo(response);
    }
}