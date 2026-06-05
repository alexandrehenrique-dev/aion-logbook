package br.com.byop.aionlogbook.notification.api;

import br.com.byop.aionlogbook.notification.application.NotificationService;
import br.com.byop.aionlogbook.notification.dto.NotificationResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public NotificationController(
            NotificationService notificationService,
            CurrentUserService currentUserService
    ) {
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<NotificationResponse> list() {
        var userId = currentUserService.currentUserId();
        return notificationService.listForUser(userId);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable UUID id) {
        var userId = currentUserService.currentUserId();
        notificationService.markRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        var userId = currentUserService.currentUserId();
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }
}
