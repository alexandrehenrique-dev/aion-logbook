package br.com.byop.aionlogbook.direction.api;

import br.com.byop.aionlogbook.direction.application.DirectionService;
import br.com.byop.aionlogbook.direction.application.DirectionSummaryService;
import br.com.byop.aionlogbook.direction.domain.DirectionStatus;
import br.com.byop.aionlogbook.direction.dto.CreateDirectionRequest;
import br.com.byop.aionlogbook.direction.dto.DirectionResponse;
import br.com.byop.aionlogbook.direction.dto.DirectionSummaryResponse;
import br.com.byop.aionlogbook.direction.dto.UpdateDirectionRequest;
import br.com.byop.aionlogbook.direction.mapper.DirectionMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/directions")
public class DirectionController {

    private final DirectionService service;
    private final DirectionSummaryService summaryService;

    public DirectionController(DirectionService service, DirectionSummaryService summaryService) {
        this.service = service;
        this.summaryService = summaryService;
    }

    @GetMapping
    public List<DirectionResponse> findAll(
            @RequestParam(required = false) DirectionStatus status
    ) {
        return service.findAll(status)
                .stream()
                .map(DirectionMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectionResponse create(@Valid @RequestBody CreateDirectionRequest request) {
        return DirectionMapper.toResponse(service.create(request));
    }

    @GetMapping("/{id}")
    public DirectionResponse findById(@PathVariable UUID id) {
        return DirectionMapper.toResponse(service.findById(id));
    }

    @GetMapping("/{id}/summary")
    public DirectionSummaryResponse getSummary(@PathVariable UUID id) {
        return summaryService.getSummary(id);
    }

    @PutMapping("/{id}")
    public DirectionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDirectionRequest request
    ) {
        return DirectionMapper.toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable UUID id) {
        service.archive(id);
    }
}
