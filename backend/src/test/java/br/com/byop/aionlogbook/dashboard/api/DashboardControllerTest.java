package br.com.byop.aionlogbook.dashboard.api;

import br.com.byop.aionlogbook.dashboard.application.DashboardService;
import br.com.byop.aionlogbook.dashboard.dto.DashboardSummaryResponse;
import br.com.byop.aionlogbook.dashboard.dto.DashboardTodayResponse;
import br.com.byop.aionlogbook.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private CurrentUserService currentUserService;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(
                dashboardService,
                currentUserService
        );
    }

    @Nested
    class Today {

        @Test
        void shouldReturnTodayDashboardForAuthenticatedUser() {
            UUID userId = UUID.randomUUID();

            var expected = new DashboardTodayResponse(
                    LocalDate.of(2026, 6, 4),
                    "Bom dia",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    90,
                    75.0,
                    2L,
                    List.of(),
                    List.of()
            );

            when(currentUserService.currentUserId()).thenReturn(userId);
            when(dashboardService.today(userId)).thenReturn(expected);

            var response = controller.today();

            assertThat(response).isEqualTo(expected);

            verify(currentUserService).currentUserId();
            verify(dashboardService).today(userId);
        }
    }

    @Nested
    class Summary {

        @Test
        void shouldReturnSummaryDashboardForAuthenticatedUser() {
            UUID userId = UUID.randomUUID();

            var expected = new DashboardSummaryResponse(
                    600,
                    70.0,
                    10L,
                    7L,
                    3L,
                    180,
                    0,
                    OffsetDateTime.parse("2026-06-04T09:00:00-03:00")
            );

            when(currentUserService.currentUserId()).thenReturn(userId);
            when(dashboardService.summary(userId)).thenReturn(expected);

            var response = controller.summary();

            assertThat(response).isEqualTo(expected);

            verify(currentUserService).currentUserId();
            verify(dashboardService).summary(userId);
        }
    }
}
