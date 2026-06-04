package br.com.byop.aionlogbook.bugreport.api;

import br.com.byop.aionlogbook.bugreport.application.CreateBugReportUseCase;
import br.com.byop.aionlogbook.bugreport.dto.BugReportResponse;
import br.com.byop.aionlogbook.bugreport.dto.CreateBugReportRequest;
import br.com.byop.aionlogbook.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bug-reports")
@RequiredArgsConstructor
public class BugReportController {

    private final CreateBugReportUseCase createBugReportUseCase;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BugReportResponse create(@Valid @RequestBody CreateBugReportRequest request) {
        var userId = currentUserService.currentUserId();

        return createBugReportUseCase.execute(userId, request);
    }
}
