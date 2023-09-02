package ir.baho.framework.config;

import ir.baho.framework.converter.PersianStringConverter;
import ir.baho.framework.converter.SearchConverter;
import ir.baho.framework.converter.SortConverter;
import ir.baho.framework.service.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CurrentUser currentUser;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PersianStringConverter());
        registry.addConverter(new SortConverter());
        registry.addConverter(new SearchConverter());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MetadataArgumentResolver(currentUser));
    }

}
