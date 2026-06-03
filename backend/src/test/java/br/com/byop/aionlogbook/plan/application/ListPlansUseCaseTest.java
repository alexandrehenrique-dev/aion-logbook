package br.com.byop.aionlogbook.plan.application;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.plan.mapper.PlanMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListPlansUseCaseTest {

    private final PlanRepository planRepository = mock(PlanRepository.class);
    private final PlanMapper planMapper = new PlanMapper();

    private final ListPlansUseCase useCase = new ListPlansUseCase(
            planRepository,
            planMapper
    );

    @Test
    void shouldListPlansByUserWhenThereAreNoFilters() {
        var userId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var plan = plan(userId, UUID.randomUUID(), PlanStatus.DRAFT, null, null);

        when(planRepository.findByFilters(userId, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(plan), pageable, 1));

        var result = useCase.execute(userId, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(plan.getId());
        assertThat(result.getContent().getFirst().status()).isEqualTo(PlanStatus.DRAFT);

        verify(planRepository).findByFilters(userId, null, null, null, pageable);
        verifyNoMoreInteractions(planRepository);
    }

    @Test
    void shouldListPlansByStatus() {
        var userId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var plan = plan(userId, UUID.randomUUID(), PlanStatus.DUE, null, null);

        when(planRepository.findByFilters(userId, PlanStatus.DUE, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(plan), pageable, 1));

        var result = useCase.execute(userId, PlanStatus.DUE, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo(PlanStatus.DUE);

        verify(planRepository).findByFilters(userId, PlanStatus.DUE, null, null, pageable);
        verifyNoMoreInteractions(planRepository);
    }

    @Test
    void shouldListPlansByDirectionId() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var plan = plan(userId, UUID.randomUUID(), PlanStatus.DRAFT, directionId, null);

        when(planRepository.findByFilters(userId, null, directionId, null, pageable))
                .thenReturn(new PageImpl<>(List.of(plan), pageable, 1));

        var result = useCase.execute(userId, null, directionId, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().directionId()).isEqualTo(directionId);

        verify(planRepository).findByFilters(userId, null, directionId, null, pageable);
        verifyNoMoreInteractions(planRepository);
    }

    @Test
    void shouldListPlansByPlannedDate() {
        var userId = UUID.randomUUID();
        var plannedDate = LocalDate.parse("2026-06-03");
        var pageable = PageRequest.of(0, 10);
        var plan = plan(userId, UUID.randomUUID(), PlanStatus.PENDING, null, plannedDate);

        when(planRepository.findByFilters(userId, null, null, plannedDate, pageable))
                .thenReturn(new PageImpl<>(List.of(plan), pageable, 1));

        var result = useCase.execute(userId, null, null, plannedDate, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().plannedDate()).isEqualTo(plannedDate);

        verify(planRepository).findByFilters(userId, null, null, plannedDate, pageable);
        verifyNoMoreInteractions(planRepository);
    }

    @Test
    void shouldListPlansByAllFilters() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var plannedDate = LocalDate.parse("2026-06-03");
        var pageable = PageRequest.of(0, 10);
        var plan = plan(userId, UUID.randomUUID(), PlanStatus.DUE, directionId, plannedDate);

        when(planRepository.findByFilters(
                userId,
                PlanStatus.DUE,
                directionId,
                plannedDate,
                pageable
        )).thenReturn(new PageImpl<>(List.of(plan), pageable, 1));

        var result = useCase.execute(
                userId,
                PlanStatus.DUE,
                directionId,
                plannedDate,
                pageable
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo(PlanStatus.DUE);
        assertThat(result.getContent().getFirst().directionId()).isEqualTo(directionId);
        assertThat(result.getContent().getFirst().plannedDate()).isEqualTo(plannedDate);

        verify(planRepository).findByFilters(
                userId,
                PlanStatus.DUE,
                directionId,
                plannedDate,
                pageable
        );
        verifyNoMoreInteractions(planRepository);
    }

    @Test
    void shouldListPlansByStatusAndPlannedDate() {
        var userId = UUID.randomUUID();
        var plannedDate = LocalDate.parse("2026-06-03");
        var pageable = PageRequest.of(0, 10);
        var plan = plan(userId, UUID.randomUUID(), PlanStatus.DUE, null, plannedDate);

        when(planRepository.findByFilters(userId, PlanStatus.DUE, null, plannedDate, pageable))
                .thenReturn(new PageImpl<>(List.of(plan), pageable, 1));

        var result = useCase.execute(userId, PlanStatus.DUE, null, plannedDate, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().status()).isEqualTo(PlanStatus.DUE);
        assertThat(result.getContent().getFirst().plannedDate()).isEqualTo(plannedDate);

        verify(planRepository).findByFilters(userId, PlanStatus.DUE, null, plannedDate, pageable);
        verifyNoMoreInteractions(planRepository);
    }

    private static Plan plan(
            UUID userId,
            UUID planId,
            PlanStatus status,
            UUID directionId,
            LocalDate plannedDate
    ) {
        var now = Instant.parse("2026-06-03T12:00:00Z");

        var plan = new Plan();
        plan.setId(planId);
        plan.setUserId(userId);
        plan.setDirectionId(directionId);
        plan.setTitle("Plano de teste");
        plan.setDescription("Descrição de teste");
        plan.setType("STUDY");
        plan.setPriority(Priority.MEDIUM);
        plan.setStatus(status);
        plan.setPlannedDate(plannedDate);
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
