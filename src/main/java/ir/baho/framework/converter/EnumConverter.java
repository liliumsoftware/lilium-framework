package ir.baho.framework.converter;

import ir.baho.framework.dto.BaseDtoSimple;
import ir.baho.framework.dto.EnumDto;
import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.enumeration.EnumValue;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.i18n.Strings;
import ir.baho.framework.service.CurrentUser;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.sf.dynamicreports.report.definition.ReportParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor
@AllArgsConstructor
public class EnumConverter<E extends Enum<E>> extends StringConverter<E> {

    @Autowired
    protected CurrentUser currentUser;

    @Autowired
    protected MessageResource messageResource;

    protected Class<?> type;

    public static String getPrefix(Enum<?> e) {
        Class<?> clas = e.getClass();
        String name = clas.getSimpleName();
        String prefix;
        if (clas.isMemberClass()) {
            Class<?> outer = clas.getEnclosingClass();
            prefix = BaseDtoSimple.class.isAssignableFrom(outer) && StringUtils.endsWithIgnoreCase(outer.getSimpleName(), "dto") ?
                    (outer.getSimpleName().substring(0, outer.getSimpleName().length() - 3) + "." + name) : (outer.getSimpleName() + "." + name);
        } else {
            prefix = EnumDto.class.isAssignableFrom(clas) && StringUtils.endsWithIgnoreCase(name, "dto") ?
                    name.substring(0, name.length() - 3) : name;
        }
        return prefix;
    }

    public static <E extends Enum<E>> E getEnum(MessageResource messageResource, EnumType enumType, Class<E> type, String value) {
        if (enumType != null) {
            switch (enumType) {
                case NAME -> {
                    return EnumSet.allOf(type).stream().filter(e -> value.equals(e.name()))
                            .findAny().orElseThrow(() -> new IllegalArgumentException("No item found for value: " + value));
                }
                case TEXT -> {
                    return EnumSet.allOf(type).stream().filter(e -> value.equals(messageResource.getMessageOrDefault(EnumConverter.getPrefix(e) + "." + e.name(), e.name())))
                            .findAny().orElseThrow(() -> new IllegalArgumentException("No item found for value: " + value));
                }
            }
        }
        if (EnumValue.class.isAssignableFrom(type)) {
            return EnumSet.allOf(type).stream().filter(e -> value.equals(String.valueOf(((EnumValue<?>) e).getValue())))
                    .findAny().orElseThrow(() -> new IllegalArgumentException("No item found for value: " + value));
        } else {
            return EnumSet.allOf(type).stream().filter(e -> value.equals(e.name()))
                    .findAny().orElseThrow(() -> new IllegalArgumentException("No item found for value: " + value));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E convert(String source) {
        if (source.isBlank() || source.equals("null")) {
            return null;
        }
        return getEnum(messageResource, currentUser.enumType(), (Class<E>) getType(), source);
    }

    @Override
    public String format(Object value, ReportParameters reportParameters) {
        E e = convert(String.valueOf(value));
        if (e == null) {
            return null;
        }
        return print(e, reportParameters.getLocale());
    }

    @Override
    public String print(E value, Locale locale) {
        return Strings.getText(messageResource.getMessageOrDefault(getPrefix(value) + "." + value.name(), value.name()), locale);
    }

    @Override
    protected List<Class<?>> supportedTypes() {
        if (type != null) {
            return List.of(type);
        }
        return super.supportedTypes();
    }

}
