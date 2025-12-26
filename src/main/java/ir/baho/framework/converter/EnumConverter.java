package ir.baho.framework.converter;

import ir.baho.framework.dto.BaseIdDto;
import ir.baho.framework.dto.EnumDto;
import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.enumeration.EnumValue;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.i18n.Strings;
import ir.baho.framework.report.ReportParameters;
import ir.baho.framework.service.CurrentUser;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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
        if (clas.isMemberClass()) {
            return getMemberClassPrefix(clas, name);
        } else {
            return getSimpleClassPrefix(clas, name);
        }
    }

    private static String getMemberClassPrefix(Class<?> clas, String name) {
        Class<?> outer = clas.getEnclosingClass();
        return BaseIdDto.class.isAssignableFrom(outer) && StringUtils.endsWithIgnoreCase(outer.getSimpleName(), "dto") ?
                (outer.getSimpleName().substring(0, outer.getSimpleName().length() - 3) + "." + name) :
                (outer.getSimpleName() + "." + name);
    }

    private static String getSimpleClassPrefix(Class<?> clas, String name) {
        return EnumDto.class.isAssignableFrom(clas) && StringUtils.endsWithIgnoreCase(name, "dto") ?
                name.substring(0, name.length() - 3) : name;
    }

    public static <E extends Enum<E>> E getEnum(MessageResource messageResource, EnumType enumType, Class<E> type, String value) {
        if (enumType != null) {
            return getEnumForEnumType(messageResource, enumType, type, value);
        }
        return getEnumForEnumValue(messageResource, type, value);
    }

    private static <E extends Enum<E>> E getEnumForEnumType(MessageResource messageResource, EnumType enumType, Class<E> type, String value) {
        return switch (enumType) {
            case NAME -> getEnumByName(type, value);
            case TEXT -> getEnumByText(messageResource, type, value);
            default -> throw new IllegalArgumentException("Unknown EnumType: " + enumType);
        };
    }

    private static <E extends Enum<E>> E getEnumByName(Class<E> type, String value) {
        return EnumSet.allOf(type).stream()
                .filter(e -> value.equals(e.name()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No item found for value: " + value));
    }

    private static <E extends Enum<E>> E getEnumByText(MessageResource messageResource, Class<E> type, String value) {
        Optional<E> optionalEnum = EnumSet.allOf(type).stream()
                .filter(e -> value.equals(messageResource.getMessageOrDefault(getPrefix(e) + "." + e.name(), e.name())))
                .findAny();
        if (optionalEnum.isEmpty() && EnumValue.class.isAssignableFrom(type)) {
            optionalEnum = EnumSet.allOf(type).stream()
                    .filter(e -> value.equals(String.valueOf(((EnumValue<?>) e).getValue())))
                    .findAny();
        }
        return optionalEnum.orElseGet(() -> getEnumByName(type, value));
    }

    private static <E extends Enum<E>> E getEnumForEnumValue(MessageResource messageResource, Class<E> type, String value) {
        Optional<E> optionalEnum = EnumSet.allOf(type).stream()
                .filter(e -> value.equals(String.valueOf(((EnumValue<?>) e).getValue())))
                .findAny();
        if (optionalEnum.isEmpty()) {
            optionalEnum = EnumSet.allOf(type).stream()
                    .filter(e -> value.equals(messageResource.getMessageOrDefault(getPrefix(e) + "." + e.name(), e.name())))
                    .findAny();
        }
        return optionalEnum.orElseGet(() -> getEnumByName(type, value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public E convert(String source) {
        if (source.isBlank() || source.equals("null")) {
            return null;
        }
        EnumType type = getEnumTypeFromCurrentUser();
        return getEnum(messageResource, type, (Class<E>) getType(), source);
    }

    private EnumType getEnumTypeFromCurrentUser() {
        if (currentUser.enumType() == null && getCurrentUser() != null) {
            return getCurrentUser().enumType();
        }
        return currentUser.enumType();
    }

    @Override
    public String format(Object value, ReportParameters reportParameters) {
        E e = convert(String.valueOf(value));
        if (e == null) {
            return null;
        }
        Locale locale = Objects.equals(reportParameters.getParameterValue(DIGITS_UNICODE), true) ? reportParameters.getLocale() : Locale.ENGLISH;
        return print(e, locale);
    }

    @Override
    public String print(E value, Locale locale) {
        return Strings.getText(messageResource.getMessageOrDefault(getPrefix(value) + "." + value.name(), value.name()), locale);
    }

    @Override
    protected List<Class<?>> supportedTypes() {
        return (type != null) ? List.of(type) : super.supportedTypes();
    }

}
