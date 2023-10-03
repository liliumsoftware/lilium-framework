package ir.baho.framework.config;

import ir.baho.framework.metadata.EnversMetadata;
import ir.baho.framework.metadata.EnversPageMetadata;
import ir.baho.framework.metadata.Metadata;
import ir.baho.framework.metadata.PageMetadata;
import ir.baho.framework.metadata.ProjectionPageMetadata;
import ir.baho.framework.metadata.ReportMetadata;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.service.impl.OptionsCurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class MetadataArgumentResolver implements HandlerMethodArgumentResolver {

    private final CurrentUser currentUser;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Metadata.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Metadata metadata = getMetadata(parameter);
        metadata.setCurrentUser(new OptionsCurrentUser(currentUser.getOptions()));

        WebDataBinder binder = binderFactory.createBinder(webRequest, metadata, parameter.getParameterName());
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest != null) {
            ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
            servletBinder.bind(servletRequest);
        }
        return binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
    }

    private Metadata getMetadata(MethodParameter parameter) {
        Metadata metadata;
        if (parameter.getParameterType().equals(PageMetadata.class)) {
            metadata = new PageMetadata();
        } else if (parameter.getParameterType().equals(ProjectionPageMetadata.class)) {
            metadata = new ProjectionPageMetadata();
        } else if (parameter.getParameterType().equals(ProjectionPageMetadata.class)) {
            metadata = new ProjectionPageMetadata();
        } else if (parameter.getParameterType().equals(ReportMetadata.class)) {
            metadata = new ReportMetadata();
        } else if (parameter.getParameterType().equals(EnversMetadata.class)) {
            metadata = new EnversMetadata();
        } else if (parameter.getParameterType().equals(EnversPageMetadata.class)) {
            metadata = new EnversPageMetadata();
        } else {
            metadata = new Metadata();
        }
        return metadata;
    }

}
