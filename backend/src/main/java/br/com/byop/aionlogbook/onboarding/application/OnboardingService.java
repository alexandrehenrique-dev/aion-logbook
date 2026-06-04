package br.com.byop.aionlogbook.onboarding.application;

import br.com.byop.aionlogbook.direction.application.DirectionService;
import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.dto.DirectionResponse;
import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.onboarding.dto.CompleteOnboardingResponse;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsRequest;
import br.com.byop.aionlogbook.onboarding.dto.CreateOnboardingDirectionsResponse;
import br.com.byop.aionlogbook.onboarding.dto.OnboardingStatusResponse;
import br.com.byop.aionlogbook.onboarding.dto.SuggestedDirectionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OnboardingService {

    private static final List<SuggestedDirectionResponse> SUGGESTED_DIRECTIONS = List.of(
            new SuggestedDirectionResponse(
                    "Estudos",
                    "Aprendizados, cursos, leituras e evolução intelectual.",
                    "#6366F1",
                    "book-open",
                    "Eu cultivo conhecimento com constância."
            ),
            new SuggestedDirectionResponse(
                    "Carreira",
                    "Metas profissionais, evolução técnica e decisões de trabalho.",
                    "#0EA5E9",
                    "briefcase",
                    "Eu construo minha trajetória com intenção."
            ),
            new SuggestedDirectionResponse(
                    "Escrita",
                    "Textos, reflexões, livros, ideias e registros autorais.",
                    "#F59E0B",
                    "pen-line",
                    "Eu transformo pensamento em palavra."
            ),
            new SuggestedDirectionResponse(
                    "Projetos",
                    "Sistemas, produtos, experimentos e obras em andamento.",
                    "#EF4444",
                    "folder-kanban",
                    "Eu termino o que merece existir."
            ),
            new SuggestedDirectionResponse(
                    "Saúde",
                    "Corpo, mente, rotina, descanso e equilíbrio pessoal.",
                    "#22C55E",
                    "heart-pulse",
                    "Eu cuido do templo que sustenta minha jornada."
            ),
            new SuggestedDirectionResponse(
                    "Filosofia",
                    "Sentido, espiritualidade, autoconhecimento e visão de mundo.",
                    "#8B5CF6",
                    "landmark",
                    "Eu observo a vida com profundidade."
            )
    );

    private final UserProfileService userProfileService;
    private final DirectionService directionService;

    public OnboardingService(
            UserProfileService userProfileService,
            DirectionService directionService
    ) {
        this.userProfileService = userProfileService;
        this.directionService = directionService;
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getStatus() {
        UserProfile profile = userProfileService.getOrCreateCurrentUserProfile();

        return new OnboardingStatusResponse(
                profile.isOnboardingCompleted(),
                SUGGESTED_DIRECTIONS
        );
    }

    @Transactional
    public CompleteOnboardingResponse complete() {
        UserProfile profile = userProfileService.getOrCreateCurrentUserProfile();
        profile.completeOnboarding();

        return new CompleteOnboardingResponse(profile.isOnboardingCompleted());
    }

    @Transactional
    public CreateOnboardingDirectionsResponse createDirections(CreateOnboardingDirectionsRequest request) {
        List<DirectionResponse> directions = request.directions()
                .stream()
                .map(directionService::create)
                .map(this::toDirectionResponse)
                .toList();

        return new CreateOnboardingDirectionsResponse(directions.size(), directions);
    }

    private DirectionResponse toDirectionResponse(Direction direction) {
        return new DirectionResponse(
                direction.getId(),
                direction.getName(),
                direction.getDescription(),
                direction.getColor(),
                direction.getIcon(),
                direction.getStatus(),
                direction.getIdentityPhrase(),
                direction.getArchivedAt(),
                direction.getCreatedAt(),
                direction.getUpdatedAt()
        );
    }
}