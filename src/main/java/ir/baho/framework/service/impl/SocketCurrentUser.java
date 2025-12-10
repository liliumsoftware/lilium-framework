package ir.baho.framework.service.impl;

import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import ir.baho.framework.web.Headers;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SocketCurrentUser implements CurrentUser {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final JsonMapper MAPPER = new JsonMapper();

    private final Map<?, ?> map = new HashMap<>();

    @SuppressWarnings("unchecked")
    public SocketCurrentUser(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null) {
            Map<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams().toSingleValueMap();
            String token = queryParams.get("authorization");
            if (token != null) {
                try {
                    map.putAll(MAPPER.readValue(DECODER.decode(token.split("\\.")[1]), Map.class));
                } catch (JacksonException _) {
                }
            }
        }
    }

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
        String locale = Optional.ofNullable(getTokenValue("locale")).orElse(getTokenValue("Accept-Language"));
        if (locale != null) {
            return Locale.of(locale);
        }
        return null;
    }

    @Override
    public ZoneId zoneId() {
        String zone = Optional.ofNullable(getTokenValue("zoneinfo")).orElse(getTokenValue(Headers.TIME_ZONE));
        if (zone != null) {
            return ZoneId.of(zone);
        }
        return null;
    }

    @Override
    public CalendarType calendarType() {
        String calendar = Optional.ofNullable(getTokenValue("calendar")).orElse(getTokenValue(Headers.CALENDAR_TYPE));
        if (calendar != null) {
            try {
                return CalendarType.valueOf(calendar);
            } catch (IllegalArgumentException _) {
            }
        }
        return null;
    }

    @Override
    public String dateFormat() {
        return getTokenValue(Headers.DATE_FORMAT);
    }

    @Override
    public String dateTimeFormat() {
        return getTokenValue(Headers.DATETIME_FORMAT);
    }

    @Override
    public String timeFormat() {
        return getTokenValue(Headers.TIME_FORMAT);
    }

    @Override
    public DurationType durationType() {
        String type = getTokenValue(Headers.DURATION_TYPE);
        if (type != null) {
            try {
                return DurationType.valueOf(type);
            } catch (IllegalArgumentException _) {
            }
        }
        return null;
    }

    @Override
    public EnumType enumType() {
        String type = getTokenValue(Headers.ENUM_TYPE);
        if (type != null) {
            try {
                return EnumType.valueOf(type);
            } catch (IllegalArgumentException _) {
            }
        }
        return null;
    }

    @Override
    public List<String> roles() {
        Map<String, Map<String, List<String>>> value = getToken();
        return Optional.of(value).map(m -> m.get("realm_access")).map(m -> m.get("roles")).orElse(List.of());
    }

    @Override
    public List<String> groups() {
        Map<String, List<String>> value = getToken();
        return Optional.of(value).map(v -> v.get("groups")).orElse(List.of());
    }

    @Override
    public List<String> scopes() {
        Map<String, String> value = getToken();
        return Optional.of(value).map(v -> v.get("scope")).map(v -> v.split(" "))
                .map(Arrays::asList).orElse(List.of());
    }

    @Override
    public List<String> permissions() {
        Map<String, Map<String, Map<String, List<String>>>> value = getToken();
        return Optional.of(value).map(m -> m.get("resource_access")).map(r -> r.values().stream()
                .flatMap(account -> account.values().stream()).flatMap(Collection::stream).toList()).orElse(List.of());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getValue(String name) {
        Map<String, ?> value = getToken();
        return (V) Optional.of(value).map(v -> v.get(name)).orElse(null);
    }

    private String getTokenValue(String name) {
        Map<String, String> value = getToken();
        return Optional.of(value).map(v -> v.get(name)).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> getToken() {
        return (Map<K, V>) map;
    }

}
