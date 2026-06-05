package br.com.byop.aionlogbook.notification.application;

import br.com.byop.aionlogbook.notification.domain.Notification;
import br.com.byop.aionlogbook.notification.dto.NotificationResponse;
import br.com.byop.aionlogbook.notification.infrastructure.NotificationRepository;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    static final String TYPE_PLAN_REMINDER = "PLAN_REMINDER";
    private static final String RELATED_ENTITY_PLAN = "PLAN";

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void markRead(UUID id, UUID userId) {
        var notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean reminderAlreadySent(UUID planId, UUID userId) {
        return notificationRepository.existsByUserIdAndRelatedEntityIdAndType(userId, planId, TYPE_PLAN_REMINDER);
    }

    @Transactional
    public void deleteReminderForPlan(UUID planId, UUID userId) {
        notificationRepository.deleteByUserIdAndRelatedEntityIdAndType(userId, planId, TYPE_PLAN_REMINDER);
    }

    @Transactional
    public void createPlanReminder(Plan plan, Instant now) {
        var notification = new Notification();
        notification.setUserId(plan.getUserId());
        notification.setType(TYPE_PLAN_REMINDER);
        notification.setTitle("Seu plano está chegando");
        notification.setMessage("Em breve: " + plan.getTitle());
        notification.setRead(false);
        notification.setRelatedEntityType(RELATED_ENTITY_PLAN);
        notification.setRelatedEntityId(plan.getId());
        notification.setCreatedAt(now);
        notificationRepository.save(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                mapType(n.getType()),
                !n.isRead(),
                n.getRelatedEntityId(),
                n.getCreatedAt()
        );
    }

    private static String mapType(String backendType) {
        return switch (backendType) {
            case "PLAN_REMINDER" -> "info";
            case "PLAN_DUE"      -> "due";
            case "PLAN_MISSED"   -> "missed";
            default              -> "info";
        };
    }
}
