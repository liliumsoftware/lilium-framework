package ir.baho.framework.i18n;

import ir.baho.framework.service.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;

@RequiredArgsConstructor
public class LocaleResolver extends AbstractLocaleContextResolver {

    private final CurrentUser currentUser;

    @Override
    public LocaleContext resolveLocaleContext(HttpServletRequest request) {
        return currentUser;
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
    }

}
