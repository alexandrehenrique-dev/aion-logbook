package br.com.byop.aionlogbook.onboarding.application;

import br.com.byop.aionlogbook.direction.application.DirectionService;
import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.dto.CreateDirectionRequest;
import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.onboarding.dto.CompleteOnboardingResponse;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsRequest;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsResponse;
import br.com.byop.aionlogbook.onboarding.dto.OnboardingStatusResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private DirectionService directionService;

    @InjectMocks
    private OnboardingService service;

    @Nested
    class GetStatus {

        @Test
        void shouldReturnOnboardingStatusWithSuggestedDirections() {
            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            OnboardingStatusResponse response = service.getStatus();

            assertThat(response.completed()).isFalse();
            assertThat(response.suggestedDirections()).hasSize(6);
            assertThat(response.suggestedDirections())
                    .extracting("name")
                    .containsExactly(
                            "Estudos",
                            "Carreira",
                            "Escrita",
                            "Projetos",
                            "Saúde",
                            "Filosofia"
                    );
        }
    }

    @Nested
    class Complete {

        @Test
        void shouldCompleteOnboarding() {
            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(profile);

            CompleteOnboardingResponse response = service.complete();

            assertThat(response.completed()).isTrue();
            assertThat(profile.isOnboardingCompleted()).isTrue();
        }
    }

    @Nested
    class CreateDirections {

        @Test
        void shouldCreateDirectionsInBatch() {
            OffsetDateTime now = OffsetDateTime.parse("2026-06-04T10:00:00Z");

            UserProfile profile = new UserProfile(
                    "keycloak-subject",
                    "loki@email.com",
                    "loki",
                    "Loki"
            );

            CreateDirectionRequest estudosRequest = new CreateDirectionRequest(
                    "Estudos",
                    "Aprendizados e leituras.",
                    "#6366F1",
                    "book-open",
                    "Eu cultivo conhecimento com constância."
            );

            CreateDirectionRequest carreiraRequest = new CreateDirectionRequest(
                    "Carreira",
                    "Evolução profissional.",
                    "#0EA5E9",
                    "briefcase",
                    "Eu construo minha trajetória com intenção."
            );

            Direction estudos = new Direction(
                    profile,
                    estudosRequest.name(),
                    estudosRequest.description(),
                    estudosRequest.color(),
                    estudosRequest.icon(),
                    estudosRequest.identityPhrase(),
                    now
            );

            Direction carreira = new Direction(
                    profile,
                    carreiraRequest.name(),
                    carreiraRequest.description(),
                    carreiraRequest.color(),
                    carreiraRequest.icon(),
                    carreiraRequest.identityPhrase(),
                    now
            );

            CreateOnboardingDirectionsRequest request = new CreateOnboardingDirectionsRequest(
                    List.of(estudosRequest, carreiraRequest)
            );

            when(directionService.create(estudosRequest)).thenReturn(estudos);
            when(directionService.create(carreiraRequest)).thenReturn(carreira);

            CreateOnboardingDirectionsResponse response = service.createDirections(request);

            assertThat(response.created()).isEqualTo(2);
            assertThat(response.directions()).hasSize(2);

            assertThat(response.directions().getFirst().name()).isEqualTo("Estudos");
            assertThat(response.directions().getFirst().status()).isEqualTo(DirectionStatus.ACTIVE);

            assertThat(response.directions().get(1).name()).isEqualTo("Carreira");
            assertThat(response.directions().get(1).status()).isEqualTo(DirectionStatus.ACTIVE);

            verify(directionService).create(estudosRequest);
            verify(directionService).create(carreiraRequest);
            verify(directionService, times(2)).create(any(CreateDirectionRequest.class));
        }
    }
}