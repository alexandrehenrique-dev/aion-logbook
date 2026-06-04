package br.com.byop.aionlogbook.settings.api;

import br.com.byop.aionlogbook.settings.application.SettingsService;
import br.com.byop.aionlogbook.settings.dto.SettingsResponse;
import br.com.byop.aionlogbook.settings.dto.UpdateSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    @GetMapping
    public SettingsResponse getSettings() {
        return service.getCurrentSettings();
    }

    @PutMapping
    public SettingsResponse updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request
    ) {
        return service.update(request);
    }
}
