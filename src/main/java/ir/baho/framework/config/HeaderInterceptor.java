package ir.baho.framework.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
            Collections.list(httpServletRequest.getHeaderNames())
                    .forEach(headerName -> requestTemplate.header(headerName, httpServletRequest.getHeader(headerName)));
        }
    }

}
