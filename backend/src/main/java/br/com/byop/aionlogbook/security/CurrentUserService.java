package br.com.byop.aionlogbook.security;

import br.com.byop.aionlogbook.identity.application.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final UserProfileService userProfileService;

    public CurrentUserService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Transactional
    public UUID currentUserId() {
        return userProfileService.getOrCreateCurrentUserProfile().getId();
    }
}