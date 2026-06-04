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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectionServiceTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private DirectionRepository repository;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private DirectionService service;

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-06-02T10:00:00Z");

    @Nested
    class FindAll {

        @Test
        void shouldFindActiveDirectionsByDefault() {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findAllByUserProfileIdAndStatusOrderByNameAsc(userProfile.getId(), DirectionStatus.ACTIVE))
                    .thenReturn(List.of(direction));

            List<Direction> result = service.findAll(null);

            assertThat(result).containsExactly(direction);

            verify(repository).findAllByUserProfileIdAndStatusOrderByNameAsc(
                    userProfile.getId(),
                    DirectionStatus.ACTIVE
            );
        }

        @Test
        void shouldFindDirectionsByRequestedStatus() {
            UserProfile userProfile = userProfile();

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findAllByUserProfileIdAndStatusOrderByNameAsc(userProfile.getId(), DirectionStatus.ARCHIVED))
                    .thenReturn(List.of());

            List<Direction> result = service.findAll(DirectionStatus.ARCHIVED);

            assertThat(result).isEmpty();

            verify(repository).findAllByUserProfileIdAndStatusOrderByNameAsc(
                    userProfile.getId(),
                    DirectionStatus.ARCHIVED
            );
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldFindDirectionByIdAndCurrentUser() {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findByIdAndUserProfileId(direction.getId(), userProfile.getId()))
                    .thenReturn(Optional.of(direction));

            Direction result = service.findById(direction.getId());

            assertThat(result).isEqualTo(direction);
        }

        @Test
        void shouldThrowNotFoundWhenDirectionDoesNotBelongToCurrentUser() {
            UserProfile userProfile = userProfile();
            UUID directionId = UUID.randomUUID();

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findByIdAndUserProfileId(directionId, userProfile.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(directionId))
                    .isInstanceOf(DirectionNotFoundException.class);
        }
    }

    @Nested
    class Create {

        @Test
        void shouldCreateDirectionWithActiveStatus() {
            UserProfile userProfile = userProfile();

            CreateDirectionRequest request = new CreateDirectionRequest(
                    "Carreira",
                    "Direção profissional",
                    "#00FF99",
                    "compass",
                    "Construir com presença"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(timeProvider.now()).thenReturn(NOW);
            when(repository.save(any(Direction.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Direction result = service.create(request);

            assertThat(result.getUserProfile()).isEqualTo(userProfile);
            assertThat(result.getName()).isEqualTo("Carreira");
            assertThat(result.getDescription()).isEqualTo("Direção profissional");
            assertThat(result.getColor()).isEqualTo("#00FF99");
            assertThat(result.getIcon()).isEqualTo("compass");
            assertThat(result.getIdentityPhrase()).isEqualTo("Construir com presença");
            assertThat(result.getStatus()).isEqualTo(DirectionStatus.ACTIVE);
            assertThat(result.getCreatedAt()).isEqualTo(NOW);
            assertThat(result.getUpdatedAt()).isEqualTo(NOW);

            verify(repository).save(any(Direction.class));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateDirectionOwnedByCurrentUser() {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            UpdateDirectionRequest request = new UpdateDirectionRequest(
                    "Espiritualidade",
                    "Direção interior",
                    "#FFFFFF",
                    "lotus",
                    "Conhece-te a ti mesmo"
            );

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findByIdAndUserProfileId(direction.getId(), userProfile.getId()))
                    .thenReturn(Optional.of(direction));
            when(timeProvider.now()).thenReturn(NOW);

            Direction result = service.update(direction.getId(), request);

            assertThat(result.getName()).isEqualTo("Espiritualidade");
            assertThat(result.getDescription()).isEqualTo("Direção interior");
            assertThat(result.getColor()).isEqualTo("#FFFFFF");
            assertThat(result.getIcon()).isEqualTo("lotus");
            assertThat(result.getIdentityPhrase()).isEqualTo("Conhece-te a ti mesmo");
            assertThat(result.getUpdatedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    class Archive {

        @Test
        void shouldArchiveDirectionOwnedByCurrentUser() {
            UserProfile userProfile = userProfile();
            Direction direction = direction(userProfile);

            when(userProfileService.getOrCreateCurrentUserProfile()).thenReturn(userProfile);
            when(repository.findByIdAndUserProfileId(direction.getId(), userProfile.getId()))
                    .thenReturn(Optional.of(direction));
            when(timeProvider.now()).thenReturn(NOW);

            service.archive(direction.getId());

            assertThat(direction.getStatus()).isEqualTo(DirectionStatus.ARCHIVED);
            assertThat(direction.getArchivedAt()).isEqualTo(NOW);
            assertThat(direction.getUpdatedAt()).isEqualTo(NOW);
        }
    }

    private static Direction direction(UserProfile userProfile) {
        return new Direction(
                userProfile,
                "Carreira",
                "Direção profissional",
                "#00FF99",
                "compass",
                "Construir com presença",
                NOW
        );
    }

    private UserProfile userProfile() {
        return mock(UserProfile.class);
    }

}
