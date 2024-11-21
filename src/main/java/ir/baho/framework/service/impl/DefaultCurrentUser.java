package ir.baho.framework.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import ir.baho.framework.web.Headers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DefaultCurrentUser implements CurrentUser {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String id() {
        return Optional.ofNullable(getTokenValue("sub")).orElse(username());
    }

    @Override
    public String username() {
        return Optional.ofNullable(getTokenValue("preferred_username"))
                .or(() -> Optional.ofNullable(getTokenValue("email")))
                .orElse("anonymousUser");
    }

    @Override
    public String email() {
        return getTokenValue("email");
    }

    @Override
    public String firstName() {
        return getTokenValue("given_name");
    }

    @Override
    public String lastName() {
        return getTokenValue("family_name");
    }

    @Override
    public Locale locale() {
        String locale = Optional.ofNullable(getTokenValue("locale")).orElse(getHeaderValue("Accept-Language"));
        if (locale != null) {
            return Locale.of(locale);
        }
        return null;
    }

    @Override
    public ZoneId zoneId() {
        String zone = Optional.ofNullable(getTokenValue("zoneinfo")).orElse(getHeaderValue(Headers.TIME_ZONE));
        if (zone != null) {
            return ZoneId.of(zone);
        }
        return null;
    }

    @Override
    public CalendarType calendarType() {
        String calendar = Optional.ofNullable(getTokenValue("calendar")).orElse(getHeaderValue(Headers.CALENDAR_TYPE));
        if (calendar != null) {
            try {
                return CalendarType.valueOf(calendar);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    @Override
    public String dateFormat() {
        return getHeaderValue(Headers.DATE_FORMAT);
    }

    @Override
    public String dateTimeFormat() {
        return getHeaderValue(Headers.DATETIME_FORMAT);
    }

    @Override
    public String timeFormat() {
        return getHeaderValue(Headers.TIME_FORMAT);
    }

    @Override
    public DurationType durationType() {
        String type = getHeaderValue(Headers.DURATION_TYPE);
        if (type != null) {
            try {
                return DurationType.valueOf(type);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    @Override
    public EnumType enumType() {
        String type = getHeaderValue(Headers.ENUM_TYPE);
        if (type != null) {
            try {
                return EnumType.valueOf(type);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    @Override
    public List<String> roles() {
        Map<String, Map<String, List<String>>> value = getToken();
        return Optional.ofNullable(value).map(m -> m.get("realm_access")).map(m -> m.get("roles")).orElse(List.of());
    }

    @Override
    public List<String> groups() {
        Map<String, List<String>> value = getToken();
        return Optional.ofNullable(value).map(v -> v.get("groups")).orElse(List.of());
    }

    @Override
    public List<String> scopes() {
        Map<String, String> value = getToken();
        return Optional.ofNullable(value).map(v -> v.get("scope")).map(v -> v.split(" "))
                .map(Arrays::asList).orElse(List.of());
    }

    @Override
    public List<String> permissions() {
        Map<String, Map<String, Map<String, List<String>>>> value = getToken();
        return Optional.ofNullable(value).map(m -> m.get("resource_access")).map(r -> r.values().stream()
                .flatMap(account -> account.values().stream()).flatMap(Collection::stream).toList()).orElse(List.of());
    }

    private String getHeaderValue(String name) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getHeader(name);
        }
        return null;
    }

    private String getTokenValue(String name) {
        Map<String, String> value = getToken();
        return Optional.ofNullable(value).map(v -> v.get(name)).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> getToken() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            String header = request.getHeader("authorization");
            if (header != null) {
                try {
                    return MAPPER.readValue(DECODER.decode(header.split("\\.")[1]), Map.class);
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        }
        return null;
    }

}
