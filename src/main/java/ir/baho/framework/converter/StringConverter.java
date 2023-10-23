package ir.baho.framework.converter;

import ir.baho.framework.i18n.Strings;
import lombok.NoArgsConstructor;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.definition.ReportParameters;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@NoArgsConstructor
public abstract class StringConverter<E> extends AbstractValueFormatter<String, Object> implements Converter<String, E>, Formatter<E> {

    public StringConverter(String name) {
        super(name);
    }

    public static StringConverter<String> stringConverter() {
        return new StringConverter<>(StringConverter.class.getSimpleName()) {
            @Override
            public String convert(String source) {
                return source;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public String format(Object value, ReportParameters reportParameters) {
        if (value == null) {
            return null;
        }
        return print((E) value, reportParameters.getLocale());
    }

    @Override
    public String print(E value, Locale locale) {
        return Strings.getText(String.valueOf(value), locale);
    }

    @Override
    public E parse(String source, Locale locale) {
        return convert(source);
    }

    public Class<?> getType() {
        return supportedTypes().getFirst();
    }

    public boolean isSupported(Class<?> clas) {
        return supportedTypes().contains(clas);
    }

    protected List<Class<?>> supportedTypes() {
        return List.of(getValueClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringConverter<?> that = (StringConverter<?>) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

}
