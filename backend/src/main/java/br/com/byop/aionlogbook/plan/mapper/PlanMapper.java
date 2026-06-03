package br.com.byop.aionlogbook.plan.mapper;

import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanEvent;
import br.com.byop.aionlogbook.plan.domain.Priority;
import br.com.byop.aionlogbook.plan.dto.CreatePlanRequest;
import br.com.byop.aionlogbook.plan.dto.PlanEventResponse;
import br.com.byop.aionlogbook.plan.dto.PlanResponse;
import br.com.byop.aionlogbook.plan.dto.UpdatePlanRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PlanMapper {

    public Plan toEntity(
            CreatePlanRequest request,
            UUID userId,
            Instant now
    ) {
        var plan = new Plan();

        plan.setId(UUID.randomUUID());
        plan.setUserId(userId);
        plan.setDirectionId(request.directionId());
        plan.setTitle(request.title());
        plan.setDescription(request.description());
        plan.setType(request.type());
        plan.setPriority(request.priority() == null ? Priority.MEDIUM : request.priority());
        plan.setPlannedDate(request.plannedDate());
        plan.setPlannedStartAt(request.plannedStartAt());
        plan.setEstimatedMinutes(request.estimatedMinutes());
        plan.setNotify(request.notificationEnabled());
        plan.setNotificationDateTime(request.notificationDateTime());
        plan.setReason(request.reason());
        plan.setTags(request.tags());
        plan.setCreatedAt(now);
        plan.setUpdatedAt(now);
        plan.setLastStatusChangedAt(now);

        return plan;
    }

    public void applyUpdate(
            Plan plan,
            UpdatePlanRequest request,
            Instant now
    ) {
        if (request.directionId() != null) {
            plan.setDirectionId(request.directionId());
        }

        if (request.title() != null) {
            plan.setTitle(request.title());
        }

        if (request.description() != null) {
            plan.setDescription(request.description());
        }

        if (request.type() != null) {
            plan.setType(request.type());
        }

        if (request.priority() != null) {
            plan.setPriority(request.priority());
        }

        if (request.plannedDate() != null) {
            plan.setPlannedDate(request.plannedDate());
        }

        if (request.plannedStartAt() != null) {
            plan.setPlannedStartAt(request.plannedStartAt());
        }

        if (request.estimatedMinutes() != null) {
            plan.setEstimatedMinutes(request.estimatedMinutes());
        }

        if (request.notificationEnabled() != null) {
            plan.setNotify(request.notificationEnabled());
        }

        if (request.notificationDateTime() != null) {
            plan.setNotificationDateTime(request.notificationDateTime());
        }

        if (request.reason() != null) {
            plan.setReason(request.reason());
        }

        if (request.tags() != null) {
            plan.setTags(request.tags());
        }

        plan.setUpdatedAt(now);
    }

    public PlanResponse toResponse(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getDirectionId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getType(),
                plan.getPriority(),
                plan.getStatus(),
                plan.getPlannedDate(),
                plan.getPlannedStartAt(),
                plan.getPlannedEndAt(),
                plan.getEstimatedMinutes(),
                plan.isNotify(),
                plan.getNotificationDateTime(),
                plan.getStartedAt(),
                plan.getFinishedAt(),
                plan.getActualMinutes(),
                plan.getReason(),
                plan.getTags(),
                plan.getCreatedAt(),
                plan.getUpdatedAt(),
                plan.getLastStatusChangedAt()
        );
    }

    public PlanEventResponse toEventResponse(PlanEvent event) {
        return new PlanEventResponse(
                event.getId(),
                event.getPlanId(),
                event.getEventType(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getDescription(),
                event.getMetadata(),
                event.getCreatedAt()
        );
    }
}