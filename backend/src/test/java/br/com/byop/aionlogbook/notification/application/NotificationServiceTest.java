package br.com.byop.aionlogbook.notification.application;

import br.com.byop.aionlogbook.notification.domain.Notification;
import br.com.byop.aionlogbook.notification.infrastructure.NotificationRepository;
import br.com.byop.aionlogbook.plan.domain.Plan;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-05T12:00:00Z");

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(notificationRepository);
    }

    @Test
    void listForUserReturnsMappedNotifications() {
        var n = notification(false);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(n.getUserId()))
                .thenReturn(List.of(n));

        var result = service.listForUser(n.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).unread()).isTrue();
        assertThat(result.get(0).title()).isEqualTo(n.getTitle());
        assertThat(result.get(0).type()).isEqualTo("info");
    }

    @Test
    void markReadSetsReadTrue() {
        var userId = UUID.randomUUID();
        var n = notification(false);
        when(notificationRepository.findByIdAndUserId(n.getId(), userId))
                .thenReturn(Optional.of(n));

        service.markRead(n.getId(), userId);

        assertThat(n.isRead()).isTrue();
        verify(notificationRepository).save(n);
    }

    @Test
    void markReadThrowsWhenNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(notificationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(id, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAllReadDelegatestoRepository() {
        var userId = UUID.randomUUID();
        service.markAllRead(userId);
        verify(notificationRepository).markAllReadByUserId(userId);
    }

    @Test
    void reminderAlreadySentDelegatesToRepository() {
        var planId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(notificationRepository.existsByUserIdAndRelatedEntityIdAndType(
                userId, planId, NotificationService.TYPE_PLAN_REMINDER
        )).thenReturn(true);

        assertThat(service.reminderAlreadySent(planId, userId)).isTrue();
    }

    @Test
    void deleteReminderForPlanDelegatesToRepository() {
        var planId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        service.deleteReminderForPlan(planId, userId);

        verify(notificationRepository).deleteByUserIdAndRelatedEntityIdAndType(
                userId, planId, NotificationService.TYPE_PLAN_REMINDER
        );
    }

    @Test
    void createPlanReminderSavesCorrectFields() {
        var plan = plan();
        service.createPlanReminder(plan, NOW);

        var captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(plan.getUserId());
        assertThat(saved.getType()).isEqualTo(NotificationService.TYPE_PLAN_REMINDER);
        assertThat(saved.getTitle()).isEqualTo("Seu plano está chegando");
        assertThat(saved.getMessage()).contains(plan.getTitle());
        assertThat(saved.isRead()).isFalse();
        assertThat(saved.getRelatedEntityId()).isEqualTo(plan.getId());
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
    }

    private Notification notification(boolean read) {
        var n = new Notification();
        n.setId(UUID.randomUUID());
        n.setUserId(UUID.randomUUID());
        n.setType(NotificationService.TYPE_PLAN_REMINDER);
        n.setTitle("Seu plano está chegando");
        n.setMessage("Em breve: Plano de teste");
        n.setRead(read);
        n.setRelatedEntityType("PLAN");
        n.setRelatedEntityId(UUID.randomUUID());
        n.setCreatedAt(NOW);
        return n;
    }

    private Plan plan() {
        var p = new Plan();
        p.setId(UUID.randomUUID());
        p.setUserId(UUID.randomUUID());
        p.setTitle("Plano de teste");
        p.setStatus(PlanStatus.SCHEDULED);
        p.setPlannedStartAt(NOW.plusSeconds(600));
        p.setCreatedAt(NOW);
        p.setUpdatedAt(NOW);
        p.setLastStatusChangedAt(NOW);
        return p;
    }
}
