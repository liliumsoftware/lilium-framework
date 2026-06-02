package ir.baho.framework.tracing;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.client.ClientHttpRequestInterceptor;

/**
 * Common interceptors for outbound {@code RestClient} traffic. Centralised so every
 * outbound HTTP client (internal services, external providers, …) gets the same
 * correlation-id propagation and uniform request/response logging.
 */
@Slf4j
public final class RestClientInterceptors {

    private RestClientInterceptors() {
    }

    /**
     * Copies the MDC {@code requestId} onto the outbound request as
     * {@code X-Request-Id} when present.
     */
    public static ClientHttpRequestInterceptor propagateRequestId() {
        return (request, body, execution) -> {
            String requestId = MDC.get(TraceConstants.REQUEST_ID_MDC_KEY);
            if (requestId != null && !requestId.isBlank()) {
                request.getHeaders().add(TraceConstants.REQUEST_ID_HEADER, requestId);
            }
            return execution.execute(request, body);
        };
    }

    /**
     * Logs the request method/URI before sending, and the response status afterwards.
     */
    public static ClientHttpRequestInterceptor logExchange() {
        return (request, body, execution) -> {
            log.debug("HTTP {} {}", request.getMethod(), request.getURI());
            var response = execution.execute(request, body);
            if (response.getStatusCode().is5xxServerError()) {
                log.warn("HTTP response {}", response.getStatusCode());
            } else {
                log.debug("HTTP response {}", response.getStatusCode());
            }
            return response;
        };
    }

}
