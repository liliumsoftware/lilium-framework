package ir.baho.framework.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import ir.baho.framework.web.Headers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;

@ConditionalOnClass(name = "org.springframework.cloud.openfeign.FeignFormatterRegistrar")
@Component
@RequiredArgsConstructor
public class HeaderInterceptor implements RequestInterceptor {

    private final HttpServletRequest httpServletRequest;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (RequestContextHolder.getRequestAttributes() != null) {
            for (String headerName : Collections.list(httpServletRequest.getHeaderNames())) {
                if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)
                        || headerName.equalsIgnoreCase(Headers.TIME_ZONE)
                        || headerName.equalsIgnoreCase(Headers.CALENDAR_TYPE)
                        || headerName.equalsIgnoreCase(Headers.DATE_FORMAT)
                        || headerName.equalsIgnoreCase(Headers.DATETIME_FORMAT)
                        || headerName.equalsIgnoreCase(Headers.TIME_FORMAT)
                        || headerName.equalsIgnoreCase(Headers.DURATION_TYPE)
                        || headerName.equalsIgnoreCase(Headers.ENUM_TYPE)) {
                    requestTemplate.header(headerName, httpServletRequest.getHeader(headerName));
                }
            }
        }
    }

}
