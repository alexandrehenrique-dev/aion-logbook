package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GetPlanUseCaseTest {

    private final PlanRepository planRepository = mock(PlanRepository.class);
    private final PlanMapper planMapper = new PlanMapper();

    private final GetPlanUseCase useCase = new GetPlanUseCase(
            planRepository,
            planMapper
    );

    @Test
    void shouldGetPlanWhenItBelongsToUser() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = plan(userId, planId);

        when(planRepository.findByIdAndUserId(planId, userId))
                .thenReturn(Optional.of(plan));

        var response = useCase.execute(userId, planId);

        assertThat(response.id()).isEqualTo(planId);
        assertThat(response.title()).isEqualTo("Plano de teste");
        assertThat(response.description()).isEqualTo("Descrição de teste");
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.status()).isEqualTo(PlanStatus.DRAFT);
        assertThat(response.tags()).containsExactly("java", "spring");

        verify(planRepository).findByIdAndUserId(planId, userId);
    }

    @Test
    void shouldThrowWhenPlanDoesNotExistOrDoesNotBelongToUser() {
        var userId = UUID.randomUUID();
        var planId = UUID.randomUUID();

        when(planRepository.findByIdAndUserId(planId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId, planId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Plan not found");

        verify(planRepository).findByIdAndUserId(planId, userId);
    }

    private static Plan plan(UUID userId, UUID planId) {
        var now = Instant.parse("2026-06-03T12:00:00Z");

        var plan = new Plan();
        plan.setId(planId);
        plan.setUserId(userId);
        plan.setDirectionId(UUID.randomUUID());
        plan.setTitle("Plano de teste");
        plan.setDescription("Descrição de teste");
        plan.setType("STUDY");
        plan.setPriority(Priority.MEDIUM);
        plan.setStatus(PlanStatus.DRAFT);
        plan.setPlannedDate(LocalDate.parse("2026-06-03"));
        plan.setPlannedStartAt(null);
        plan.setPlannedEndAt(null);
        plan.setEstimatedMinutes(60);
        plan.setNotify(false);
        plan.setNotificationDateTime(null);
        plan.setStartedAt(null);
        plan.setFinishedAt(null);
        plan.setActualMinutes(null);
        plan.setReason("Motivo teste");
        plan.setTags(List.of("java", "spring"));
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        return plan;
    }
}
