package br.com.byop.aionlogbook.plan.infrastructure;

import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanEventRepository extends JpaRepository<PlanEvent, UUID> {

    List<PlanEvent> findByPlanIdAndUserIdOrderByCreatedAtAsc(UUID planId, UUID userId);
}