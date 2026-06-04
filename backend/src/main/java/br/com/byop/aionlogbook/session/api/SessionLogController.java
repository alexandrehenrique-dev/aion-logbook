package br.com.byop.aionlogbook.session.api;

import br.com.byop.aionlogbook.security.CurrentUserService;
import br.com.byop.aionlogbook.session.application.SessionLogService;
import br.com.byop.aionlogbook.session.dto.CreateSessionLogRequest;
import br.com.byop.aionlogbook.session.dto.SessionLogResponse;
import br.com.byop.aionlogbook.session.dto.UpdateSessionLogRequest;
import br.com.byop.aionlogbook.session.mapper.SessionLogMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
public class SessionLogController {

    private final SessionLogService sessionLogService;
    private final SessionLogMapper sessionLogMapper;
    private final CurrentUserService currentUserService;

    @GetMapping
    public Page<SessionLogResponse> findAll(
            @RequestParam(required = false) UUID directionId,
            @RequestParam(required = false) UUID planId,
            @RequestParam(required = false) OffsetDateTime dateFrom,
            @RequestParam(required = false) OffsetDateTime dateTo,
            Pageable pageable
    ) {
        var userId = currentUserService.currentUserId();

        return sessionLogService.findAll(
                        userId,
                        directionId,
                        planId,
                        dateFrom,
                        dateTo,
                        pageable
                )
                .map(sessionLogMapper::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionLogResponse create(
            @Valid @RequestBody CreateSessionLogRequest request
    ) {
        var userId = currentUserService.currentUserId();

        var sessionLog = sessionLogService.create(userId, request);

        return sessionLogMapper.toResponse(sessionLog);
    }

    @GetMapping("/{id}")
    public SessionLogResponse findById(
            @PathVariable UUID id
    ) {
        var userId = currentUserService.currentUserId();

        var sessionLog = sessionLogService.findById(userId, id);

        return sessionLogMapper.toResponse(sessionLog);
    }

    @PutMapping("/{id}")
    public SessionLogResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSessionLogRequest request
    ) {
        var userId = currentUserService.currentUserId();

        var sessionLog = sessionLogService.update(userId, id, request);

        return sessionLogMapper.toResponse(sessionLog);
    }
}