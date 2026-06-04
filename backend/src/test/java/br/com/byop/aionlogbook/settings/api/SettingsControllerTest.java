package br.com.byop.aionlogbook.settings.api;

import br.com.byop.aionlogbook.settings.application.SettingsService;
import br.com.byop.aionlogbook.settings.dto.SettingsResponse;
import br.com.byop.aionlogbook.settings.dto.UpdateSettingsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsControllerTest {

    @Mock
    private SettingsService service;

    @InjectMocks
    private SettingsController controller;

    @Test
    void shouldReturnSettings() {
        SettingsResponse response = new SettingsResponse(
                "UTC",
                "dark",
                60,
                true,
                10,
                false
        );

        when(service.getCurrentSettings()).thenReturn(response);

        SettingsResponse result = controller.getSettings();

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldUpdateSettings() {
        UpdateSettingsRequest request = new UpdateSettingsRequest(
                "UTC",
                "dark",
                120,
                false,
                20
        );

        SettingsResponse response = new SettingsResponse(
                "UTC",
                "dark",
                120,
                false,
                20,
                false
        );

        when(service.update(request)).thenReturn(response);

        SettingsResponse result = controller.updateSettings(request);

        assertThat(result).isEqualTo(response);
    }
}