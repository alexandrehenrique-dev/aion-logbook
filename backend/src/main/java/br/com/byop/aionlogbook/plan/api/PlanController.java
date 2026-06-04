package br.com.byop.aionlogbook.plan.api;

import br.com.byop.aionlogbook.plan.application.*;
import br.com.byop.aionlogbook.plan.domain.PlanStatus;
import br.com.byop.aionlogbook.plan.dto.*;
import br.com.byop.aionlogbook.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final CreatePlanUseCase createPlanUseCase;
    private final UpdatePlanUseCase updatePlanUseCase;
    private final GetPlanUseCase getPlanUseCase;
    private final ListPlansUseCase listPlansUseCase;
    private final GetPlanEventsUseCase getPlanEventsUseCase;
    private final CurrentUserService currentUserService;
    private final TransitionPlanUseCase transitionPlanUseCase;

    public PlanController(
            CreatePlanUseCase createPlanUseCase,
            UpdatePlanUseCase updatePlanUseCase,
            GetPlanUseCase getPlanUseCase,
            ListPlansUseCase listPlansUseCase,
            GetPlanEventsUseCase getPlanEventsUseCase,
            CurrentUserService currentUserService,
            TransitionPlanUseCase transitionPlanUseCase
    ) {
        this.createPlanUseCase = createPlanUseCase;
        this.updatePlanUseCase = updatePlanUseCase;
        this.getPlanUseCase = getPlanUseCase;
        this.listPlansUseCase = listPlansUseCase;
        this.getPlanEventsUseCase = getPlanEventsUseCase;
        this.currentUserService = currentUserService;
        this.transitionPlanUseCase = transitionPlanUseCase;
    }

    @GetMapping
    public Page<PlanResponse> list(
            @RequestParam(required = false) PlanStatus status,
            @RequestParam(required = false) UUID directionId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate plannedDate,
            Pageable pageable
    ) {
        var userId = currentUserService.currentUserId();

        return listPlansUseCase.execute(
                userId,
                status,
                directionId,
                plannedDate,
                pageable
        );
    }

    @PostMapping
    public PlanResponse create(@Valid @RequestBody CreatePlanRequest request) {
        var userId = currentUserService.currentUserId();

        return createPlanUseCase.execute(userId, request);
    }

    @GetMapping("/{id}")
    public PlanResponse getById(@PathVariable UUID id) {
        var userId = currentUserService.currentUserId();

        return getPlanUseCase.execute(userId, id);
    }

    @PutMapping("/{id}")
    public PlanResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        var userId = currentUserService.currentUserId();

        return updatePlanUseCase.execute(userId, id, request);
    }

    @GetMapping("/{id}/events")
    public List<PlanEventResponse> events(@PathVariable UUID id) {
        var userId = currentUserService.currentUserId();

        return getPlanEventsUseCase.execute(userId, id);
    }

    @PostMapping("/{id}/start")
    public PlanResponse start(
            @PathVariable UUID id,
            @Valid @RequestBody StartPlanRequest request
    ) {
        var userId = currentUserService.currentUserId();
        return transitionPlanUseCase.start(userId, id, request);
    }

    @PostMapping("/{id}/complete")
    public PlanResponse complete(
            @PathVariable UUID id,
            @Valid @RequestBody CompletePlanRequest request
    ) {
        var userId = currentUserService.currentUserId();
        return transitionPlanUseCase.complete(userId, id, request);
    }

    @PostMapping("/{id}/partial")
    public PlanResponse partial(
            @PathVariable UUID id,
            @Valid @RequestBody PartialPlanRequest request
    ) {
        var userId = currentUserService.currentUserId();
        return transitionPlanUseCase.partial(userId, id, request);
    }

    @PostMapping("/{id}/postpone")
    public PlanResponse postpone(
            @PathVariable UUID id,
            @Valid @RequestBody PostponePlanRequest request
    ) {
        var userId = currentUserService.currentUserId();
        return transitionPlanUseCase.postpone(userId, id, request);
    }

    @PostMapping("/{id}/ignore")
    public PlanResponse ignore(
            @PathVariable UUID id,
            @Valid @RequestBody IgnorePlanRequest request
    ) {
        var userId = currentUserService.currentUserId();
        return transitionPlanUseCase.ignore(userId, id, request);
    }

    @PostMapping("/{id}/cancel")
    public PlanResponse cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelPlanRequest request
    ) {
        var userId = currentUserService.currentUserId();
        return transitionPlanUseCase.cancel(userId, id, request);
    }

    @PostMapping("/{id}/modify")
    public PlanResponse modify(
            @PathVariable UUID id,
            @Valid @RequestBody ModifyPlanRequest request
    ) {
        var userId = currentUserService.currentUserId();
        return transitionPlanUseCase.modify(userId, id, request);
    }
}