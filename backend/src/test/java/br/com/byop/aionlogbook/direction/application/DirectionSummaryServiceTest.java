package br.com.byop.aionlogbook.direction.application;

import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.dto.DirectionSummaryResponse;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.session.infrastructure.SessionLogRepository;
import br.com.byop.aionlogbook.shared.error.DirectionNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectionSummaryServiceTest {

    @Mock
    private DirectionService directionService;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private SessionLogRepository sessionLogRepository;

    @InjectMocks
    private DirectionSummaryService summaryService;

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-06-02T10:00:00Z");

    @Nested
    class GetSummary {

        @Test
        void shouldReturnSummaryWithAggregatedCounts() {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);
            UUID userId = userProfile.getId();
            UUID directionId = direction.getId();

            when(directionService.findById(directionId)).thenReturn(direction);
            when(planRepository.countByUserIdAndDirectionId(userId, directionId)).thenReturn(10L);
            when(planRepository.countByUserIdAndDirectionIdAndStatusIn(eq(userId), eq(directionId), any()))
                    .thenReturn(6L, 3L);
            when(sessionLogRepository.countByUserIdAndDirectionId(userId, directionId)).thenReturn(5L);
            when(sessionLogRepository.sumDurationMinutesByUserIdAndDirectionId(userId, directionId)).thenReturn(120);

            DirectionSummaryResponse result = summaryService.getSummary(directionId);

            assertThat(result)
                    .extracting(
                            DirectionSummaryResponse::id,
                            DirectionSummaryResponse::name,
                            DirectionSummaryResponse::totalPlans,
                            DirectionSummaryResponse::completedPlans,
                            DirectionSummaryResponse::activePlans,
                            DirectionSummaryResponse::totalSessions,
                            DirectionSummaryResponse::totalSessionMinutes
                    )
                    .containsExactly(
                            direction.getId(),
                            "Carreira",
                            10L,
                            6L,
                            3L,
                            5L,
                            120L
                    );
        }

        @Test
        void shouldReturnZeroMinutesWhenNoSessionsExist() {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);
            UUID userId = userProfile.getId();
            UUID directionId = direction.getId();

            when(directionService.findById(directionId)).thenReturn(direction);
            when(planRepository.countByUserIdAndDirectionId(userId, directionId)).thenReturn(0L);
            when(planRepository.countByUserIdAndDirectionIdAndStatusIn(eq(userId), eq(directionId), any()))
                    .thenReturn(0L);
            when(sessionLogRepository.countByUserIdAndDirectionId(userId, directionId)).thenReturn(0L);
            when(sessionLogRepository.sumDurationMinutesByUserIdAndDirectionId(userId, directionId))
                    .thenReturn(null);

            DirectionSummaryResponse result = summaryService.getSummary(directionId);

            assertThat(result.totalSessionMinutes()).isZero();
        }

        @Test
        void shouldPropagateNotFoundWhenDirectionDoesNotBelongToCurrentUser() {
            UUID directionId = UUID.randomUUID();

            when(directionService.findById(directionId)).thenThrow(new DirectionNotFoundException());

            assertThatThrownBy(() -> summaryService.getSummary(directionId))
                    .isInstanceOf(DirectionNotFoundException.class);
        }
    }

    private static Direction direction(UserProfile userProfile) {
        return new Direction(
                userProfile,
                "Carreira",
                "Direção profissional",
                "#00FF99",
                "compass",
                "Construir com presença",
                NOW
        );
    }

    private UserProfile userProfile() {
        UserProfile profile = mock(UserProfile.class);
        when(profile.getId()).thenReturn(UUID.randomUUID());
        return profile;
    }
}
