package ir.baho.framework.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adopts an inbound {@code X-Request-Id} (or generates a new one), publishes it on
 * SLF4J MDC for the duration of the request, and echoes it on the response so callers
 * can correlate end-to-end. Cleared in a {@code finally} so threads in the pool don't
 * leak the value to the next request.
 */
@AutoConfiguration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    /**
     * Hard cap so a malicious client can't fill our MDC with megabyte-sized headers.
     */
    private static final int MAX_LENGTH = 128;

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) return null;
        // ASCII printable only — no CRLF / control chars (log/response injection guard).
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < 0x20 || c > 0x7E) return null;
        }
        return trimmed;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String inbound = request.getHeader(TraceConstants.REQUEST_ID_HEADER);
        String requestId = sanitize(inbound);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(TraceConstants.REQUEST_ID_MDC_KEY, requestId);
        try {
            response.setHeader(TraceConstants.REQUEST_ID_HEADER, requestId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TraceConstants.REQUEST_ID_MDC_KEY);
        }
    }

}
