package br.com.byop.aionlogbook.notification.api;

import br.com.byop.aionlogbook.notification.application.NotificationService;
import br.com.byop.aionlogbook.notification.dto.NotificationResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = NotificationController.class,
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8181/realms/aion-logbook-test",
                "app.cors.allowed-origins=http://localhost:5173"
        }
)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private CurrentUserService currentUserService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void listReturnsNotificationsForCurrentUser() throws Exception {
        var notif = new NotificationResponse(
                UUID.randomUUID(),
                "Seu plano está chegando",
                "Em breve: Plano teste",
                "info",
                true,
                UUID.randomUUID(),
                Instant.parse("2026-06-05T12:00:00Z")
        );

        when(currentUserService.currentUserId()).thenReturn(userId);
        when(notificationService.listForUser(userId)).thenReturn(List.of(notif));

        mockMvc.perform(get("/api/v1/notifications").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Seu plano está chegando"))
                .andExpect(jsonPath("$[0].unread").value(true))
                .andExpect(jsonPath("$[0].type").value("info"));
    }

    @Test
    void listReturnsEmptyWhenNoNotifications() throws Exception {
        when(currentUserService.currentUserId()).thenReturn(userId);
        when(notificationService.listForUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void markReadReturnsNoContent() throws Exception {
        var notifId = UUID.randomUUID();
        when(currentUserService.currentUserId()).thenReturn(userId);
        doNothing().when(notificationService).markRead(notifId, userId);

        mockMvc.perform(post("/api/v1/notifications/{id}/read", notifId).with(jwt()))
                .andExpect(status().isNoContent());

        verify(notificationService).markRead(notifId, userId);
    }

    @Test
    void markAllReadReturnsNoContent() throws Exception {
        when(currentUserService.currentUserId()).thenReturn(userId);
        doNothing().when(notificationService).markAllRead(userId);

        mockMvc.perform(post("/api/v1/notifications/read-all").with(jwt()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllRead(userId);
    }

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }
}
