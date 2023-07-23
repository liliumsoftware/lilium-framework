package ir.baho.framework.file;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.exception.MetadataConvertException;
import ir.baho.framework.i18n.MessageResource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Closeable;
import java.util.Collection;

@RequiredArgsConstructor
public abstract class BaseWriter implements Closeable {

    protected final MessageResource messageResource;

    @SuppressWarnings("rawtypes")
    protected final Collection<? extends StringConverter> converters;

    @SuppressWarnings("unchecked")
    protected String convert(Class<?> type, Object value) {
        return converters.stream()
                .filter(c -> c.isSupported(type)).findFirst()
                .orElseThrow(() -> new MetadataConvertException(type, value))
                .print(value, LocaleContextHolder.getLocale());
    }

}
