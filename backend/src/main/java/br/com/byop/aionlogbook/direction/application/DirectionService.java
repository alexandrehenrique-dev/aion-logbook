package br.com.byop.aionlogbook.direction.application;

import br.com.byop.aionlogbook.direction.domain.Direction;
import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.dto.CreateDirectionRequest;
import br.com.byop.aionlogbook.direction.dto.UpdateDirectionRequest;
import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.identity.application.UserProfileService;
import br.com.byop.aionlogbook.identity.domain.UserProfile;
import br.com.byop.aionlogbook.shared.error.DirectionNotFoundException;
import br.com.byop.aionlogbook.shared.time.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DirectionService {

    private final UserProfileService userProfileService;
    private final DirectionRepository repository;
    private final TimeProvider timeProvider;

    public DirectionService(
            UserProfileService userProfileService,
            DirectionRepository repository,
            TimeProvider timeProvider
    ) {
        this.userProfileService = userProfileService;
        this.repository = repository;
        this.timeProvider = timeProvider;
    }

    @Transactional(readOnly = true)
    public List<Direction> findAll(DirectionStatus status) {
        UserProfile userProfile = userProfileService.getOrCreateCurrentUserProfile();

        if (status == null) {
            return repository.findAllByUserProfileIdAndStatusOrderByNameAsc(
                    userProfile.getId(),
                    DirectionStatus.ACTIVE
            );
        }

        return repository.findAllByUserProfileIdAndStatusOrderByNameAsc(
                userProfile.getId(),
                status
        );
    }

    @Transactional(readOnly = true)
    public Direction findById(UUID id) {
        UserProfile userProfile = userProfileService.getOrCreateCurrentUserProfile();

        return repository.findByIdAndUserProfileId(id, userProfile.getId())
                .orElseThrow(DirectionNotFoundException::new);
    }

    @Transactional
    public Direction create(CreateDirectionRequest request) {
        UserProfile userProfile = userProfileService.getOrCreateCurrentUserProfile();

        Direction direction = new Direction(
                userProfile,
                request.name(),
                request.description(),
                request.color(),
                request.icon(),
                request.identityPhrase(),
                timeProvider.now()
        );

        return repository.save(direction);
    }

    @Transactional
    public Direction update(UUID id, UpdateDirectionRequest request) {
        Direction direction = findById(id);

        direction.update(
                request.name(),
                request.description(),
                request.color(),
                request.icon(),
                request.identityPhrase(),
                timeProvider.now()
        );

        return direction;
    }

    @Transactional
    public void archive(UUID id) {
        Direction direction = findById(id);
        direction.archive(timeProvider.now());
    }
}
