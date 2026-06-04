package br.com.byop.aionlogbook.logbook.application;

import br.com.byop.aionlogbook.direction.infrastructure.DirectionRepository;
import br.com.byop.aionlogbook.logbook.domain.LogEntry;
import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import br.com.byop.aionlogbook.logbook.dto.CreateLogEntryRequest;
import br.com.byop.aionlogbook.logbook.dto.LogEntryResponse;
import br.com.byop.aionlogbook.logbook.infrastructure.LogEntryRepository;
import br.com.byop.aionlogbook.logbook.mapper.LogEntryMapper;
import br.com.byop.aionlogbook.plan.infrastructure.PlanRepository;
import br.com.byop.aionlogbook.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogEntryServiceTest {

    @Mock
    private LogEntryRepository logEntryRepository;

    @Mock
    private DirectionRepository directionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private LogEntryMapper logEntryMapper;

    @Mock
    private ObjectMapper objectMapper;

    private LogEntryService service;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-06-04T10:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        service = new LogEntryService(
                logEntryRepository,
                directionRepository,
                planRepository,
                logEntryMapper,
                objectMapper,
                clock
        );
    }

    @Test
    void shouldCreateWhenDirectionAndPlanBelongToUser() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();
        var planId = UUID.randomUUID();

        var request = new CreateLogEntryRequest(
                directionId,
                planId,
                "Título",
                "Conteúdo",
                LogEntryType.REFLECTION,
                List.of("tag")
        );

        var entity = new LogEntry();
        var response = new LogEntryResponse(
                UUID.randomUUID(),
                directionId,
                planId,
                "Título",
                "Conteúdo",
                LogEntryType.REFLECTION,
                List.of("tag"),
                Instant.now(clock),
                Instant.now(clock)
        );

        when(directionRepository.existsByIdAndUserProfileId(directionId, userId)).thenReturn(true);
        when(planRepository.existsByIdAndUserId(planId, userId)).thenReturn(true);
        when(logEntryMapper.toEntity(request, userId, Instant.now(clock))).thenReturn(entity);
        when(logEntryRepository.save(entity)).thenReturn(entity);
        when(logEntryMapper.toResponse(entity)).thenReturn(response);

        var result = service.create(userId, request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowNotFoundWhenDirectionDoesNotBelongToUser() {
        var userId = UUID.randomUUID();
        var directionId = UUID.randomUUID();

        var request = new CreateLogEntryRequest(
                directionId,
                null,
                "Título",
                "Conteúdo",
                LogEntryType.REFLECTION,
                null
        );

        when(directionRepository.existsByIdAndUserProfileId(directionId, userId)).thenReturn(false);

        assertThatThrownBy(() -> service.create(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(logEntryRepository, never()).save(any());
    }

    @Test
    void shouldReturnNotFoundForAnotherUserResource() {
        var userId = UUID.randomUUID();
        var id = UUID.randomUUID();

        when(logEntryRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(userId, id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDeleteOwnedEntry() {
        var userId = UUID.randomUUID();
        var id = UUID.randomUUID();
        var entry = new LogEntry();

        when(logEntryRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entry));

        service.delete(userId, id);

        verify(logEntryRepository).delete(entry);
    }
}
