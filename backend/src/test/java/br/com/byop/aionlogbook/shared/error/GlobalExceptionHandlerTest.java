package br.com.byop.aionlogbook.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-06-02T12:00:00Z");
    private static final String REQUEST_ID = "req-123";
    private static final String PATH = "/api/test";

    private final Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(clock);

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Nested
    class Validation {

        @Test
        void shouldReturnBadRequestWithFieldDetailsWhenValidationFails() {
            MDC.put("requestId", REQUEST_ID);

            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError(
                    "request",
                    "name",
                    "",
                    false,
                    null,
                    null,
                    "nao deve estar em branco"
            ));
            bindingResult.addError(new FieldError(
                    "request",
                    "password",
                    "secret-value",
                    false,
                    null,
                    null,
                    "nao deve estar em branco"
            ));

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                    mock(MethodParameter.class),
                    bindingResult
            );

            ResponseEntity<ApiErrorResponse> response = handler.handleValidation(exception, request());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().timestamp()).isEqualTo(FIXED_INSTANT);
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().error()).isEqualTo("Bad Request");
            assertThat(response.getBody().code()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
            assertThat(response.getBody().message()).isEqualTo("Existem campos inválidos.");
            assertThat(response.getBody().path()).isEqualTo(PATH);
            assertThat(response.getBody().requestId()).isEqualTo(REQUEST_ID);
            assertThat(response.getBody().details())
                    .extracting(ApiFieldError::field, ApiFieldError::message, ApiFieldError::rejectedValue)
                    .containsExactly(
                            tuple("name", "nao deve estar em branco", ""),
                            tuple("password", "nao deve estar em branco", "[REDACTED]")
                    );
        }
    }

    @Nested
    class ResourceNotFound {

        @Test
        void shouldReturnNotFoundWhenResourceDoesNotExist() {
            MDC.put("requestId", REQUEST_ID);

            HttpServletRequest request = request();
            ResourceNotFoundException exception = new ResourceNotFoundException("Recurso não encontrado.");

            ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(exception, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().timestamp()).isEqualTo(FIXED_INSTANT);
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().error()).isEqualTo("Not Found");
            assertThat(response.getBody().code()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.name());
            assertThat(response.getBody().message()).isEqualTo("Recurso não encontrado.");
            assertThat(response.getBody().path()).isEqualTo(PATH);
            assertThat(response.getBody().requestId()).isEqualTo(REQUEST_ID);
            assertThat(response.getBody().details()).isEmpty();
        }
    }

    @Nested
    class InvalidTransition {

        @Test
        void shouldReturnBadRequestWhenTransitionIsInvalid() {
            MDC.put("requestId", REQUEST_ID);

            HttpServletRequest request = request();
            InvalidTransitionException exception = new InvalidTransitionException("Transição inválida.");

            ResponseEntity<ApiErrorResponse> response = handler.handleInvalidTransition(exception, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_TRANSITION.name());
            assertThat(response.getBody().message()).isEqualTo("Transição inválida.");
            assertThat(response.getBody().requestId()).isEqualTo(REQUEST_ID);
        }
    }

    @Nested
    class Conflict {

        @Test
        void shouldReturnConflictWhenConflictOccurs() {
            MDC.put("requestId", REQUEST_ID);

            HttpServletRequest request = request();
            ConflictException exception = new ConflictException("Conflito de estado.");

            ResponseEntity<ApiErrorResponse> response = handler.handleConflict(exception, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
            assertThat(response.getBody().code()).isEqualTo(ErrorCode.CONFLICT.name());
            assertThat(response.getBody().message()).isEqualTo("Conflito de estado.");
            assertThat(response.getBody().requestId()).isEqualTo(REQUEST_ID);
        }
    }

    @Nested
    class BusinessRule {

        @Test
        void shouldReturnUnprocessableEntityWhenBusinessRuleIsViolated() {
            MDC.put("requestId", REQUEST_ID);

            HttpServletRequest request = request();
            BusinessRuleException exception = new BusinessRuleException("Regra de negócio violada.");

            ResponseEntity<ApiErrorResponse> response = handler.handleBusinessRule(exception, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(422);
            assertThat(response.getBody().code()).isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION.name());
            assertThat(response.getBody().message()).isEqualTo("Regra de negócio violada.");
            assertThat(response.getBody().requestId()).isEqualTo(REQUEST_ID);
        }
    }

    @Nested
    class UnexpectedError {

        @Test
        void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs() {
            MDC.put("requestId", REQUEST_ID);

            HttpServletRequest request = request();
            RuntimeException exception = new RuntimeException("Erro sensível que não deve vazar.");

            ResponseEntity<ApiErrorResponse> response = handler.handleUnexpected(exception, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().code()).isEqualTo(ErrorCode.INTERNAL_ERROR.name());
            assertThat(response.getBody().message()).isEqualTo("Erro interno inesperado.");
            assertThat(response.getBody().message()).doesNotContain("Erro sensível");
            assertThat(response.getBody().requestId()).isEqualTo(REQUEST_ID);
        }
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(PATH);
        return request;
    }
}
