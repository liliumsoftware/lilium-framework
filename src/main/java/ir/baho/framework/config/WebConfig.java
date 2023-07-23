package ir.baho.framework.config;

import ir.baho.framework.converter.PersianStringConverter;
import ir.baho.framework.converter.SearchConverter;
import ir.baho.framework.converter.SortConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PersianStringConverter());
        registry.addConverter(new SortConverter());
        registry.addConverter(new SearchConverter());
    }

}
