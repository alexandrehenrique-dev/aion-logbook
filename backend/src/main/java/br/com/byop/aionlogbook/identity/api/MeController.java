package br.com.byop.aionlogbook.identity.api;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final UserProfileService userProfileService;

    public MeController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/api/v1/me")
    public MeResponse me() {
        return MeResponse.from(userProfileService.getOrCreateCurrentUserProfile());
    }
}
