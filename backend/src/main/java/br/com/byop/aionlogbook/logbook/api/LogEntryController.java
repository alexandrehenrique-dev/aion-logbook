package br.com.byop.aionlogbook.logbook.api;

import br.com.byop.aionlogbook.logbook.application.LogEntrySearchCriteria;
import br.com.byop.aionlogbook.logbook.application.LogEntryService;
import br.com.byop.aionlogbook.logbook.domain.LogEntryType;
import br.com.byop.aionlogbook.logbook.dto.*;
import br.com.byop.aionlogbook.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/logs")
public class LogEntryController {

    private final LogEntryService logEntryService;
    private final CurrentUserService currentUserService;

    public LogEntryController(
            LogEntryService logEntryService,
            CurrentUserService currentUserService
    ) {
        this.logEntryService = logEntryService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Page<LogEntryResponse> list(
            @RequestParam(required = false) LogEntryType type,
            @RequestParam(required = false) UUID directionId,
            @RequestParam(required = false) UUID planId,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant dateTo,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        var userId = currentUserService.currentUserId();
        var criteria = new LogEntrySearchCriteria(type, directionId, planId, tags, dateFrom, dateTo, q);

        return logEntryService.list(userId, criteria, pageable);
    }

    @PostMapping
    public ResponseEntity<LogEntryResponse> create(
            @Valid @RequestBody CreateLogEntryRequest request
    ) {
        var userId = currentUserService.currentUserId();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(logEntryService.create(userId, request));
    }

    @GetMapping("/{id}")
    public LogEntryResponse getById(@PathVariable UUID id) {
        var userId = currentUserService.currentUserId();

        return logEntryService.getById(userId, id);
    }

    @PutMapping("/{id}")
    public LogEntryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLogEntryRequest request
    ) {
        var userId = currentUserService.currentUserId();

        return logEntryService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        var userId = currentUserService.currentUserId();

        logEntryService.delete(userId, id);
    }
}
