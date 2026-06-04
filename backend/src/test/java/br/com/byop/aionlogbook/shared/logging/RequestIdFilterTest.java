package br.com.byop.aionlogbook.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void shouldUseProvidedRequestIdAndAddItToResponseAndMdc() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "req-abc");

        AtomicReference<String> requestIdFromMdc = new AtomicReference<>();
        AtomicReference<String> methodFromMdc = new AtomicReference<>();
        AtomicReference<String> pathFromMdc = new AtomicReference<>();

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            requestIdFromMdc.set(MDC.get("requestId"));
            methodFromMdc.set(MDC.get("method"));
            pathFromMdc.set(MDC.get("path"));
        };

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("req-abc");
        assertThat(requestIdFromMdc.get()).isEqualTo("req-abc");
        assertThat(methodFromMdc.get()).isEqualTo("GET");
        assertThat(pathFromMdc.get()).isEqualTo("/api/test");

        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("method")).isNull();
        assertThat(MDC.get("path")).isNull();
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> requestIdFromMdc = new AtomicReference<>();

        FilterChain filterChain = (servletRequest, servletResponse) ->
                requestIdFromMdc.set(MDC.get("requestId"));

        filter.doFilter(request, response, filterChain);

        String responseRequestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);

        assertThat(responseRequestId).isNotBlank();
        assertThat(requestIdFromMdc.get()).isEqualTo(responseRequestId);
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderIsBlank() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "   ");

        AtomicReference<String> requestIdFromMdc = new AtomicReference<>();

        FilterChain filterChain = (servletRequest, servletResponse) ->
                requestIdFromMdc.set(MDC.get("requestId"));

        filter.doFilter(request, response, filterChain);

        String responseRequestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);

        assertThat(responseRequestId)
                .isNotBlank()
                .isNotEqualTo("   ");
        assertThat(requestIdFromMdc.get()).isEqualTo(responseRequestId);
        assertThat(MDC.get("requestId")).isNull();
    }
}
