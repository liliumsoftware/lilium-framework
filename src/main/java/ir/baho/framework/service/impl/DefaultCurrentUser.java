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
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

public class DefaultCurrentUser implements CurrentUser {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getId() {
        return Optional.ofNullable(getToken("sub")).orElse(getUsername());
    }

    @Override
    public String getUsername() {
        return Optional.ofNullable(getToken("preferred_username")).orElse("anonymousUser");
    }

    @Override
    public Locale getLocale() {
        String locale = Optional.ofNullable(getToken("locale")).orElse(getHeader("Accept-Language"));
        if (locale != null) {
            return new Locale(locale);
        }
        return null;
    }

    @Override
    public TimeZone getTimeZone() {
        String zone = Optional.ofNullable(getToken("zoneinfo")).orElse(getHeader(Headers.TIME_ZONE));
        if (zone != null) {
            return TimeZone.getTimeZone(zone);
        }
        return null;
    }

    @Override
    public CalendarType getCalendarType() {
        String calendar = Optional.ofNullable(getToken("calendar")).orElse(getHeader(Headers.CALENDAR_TYPE));
        if (calendar != null) {
            try {
                return CalendarType.valueOf(calendar);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    @Override
    public String getDateFormat() {
        return getHeader(Headers.DATE_FORMAT);
    }

    @Override
    public String getDateTimeFormat() {
        return getHeader(Headers.DATETIME_FORMAT);
    }

    @Override
    public String getTimeFormat() {
        return getHeader(Headers.TIME_FORMAT);
    }

    @Override
    public DurationType getDurationType() {
        String type = getHeader(Headers.DURATION_TYPE);
        if (type != null) {
            try {
                return DurationType.valueOf(type);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    @Override
    public EnumType getEnumType() {
        String type = getHeader(Headers.ENUM_TYPE);
        if (type != null) {
            try {
                return EnumType.valueOf(type);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private String getHeader(String name) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getHeader(name);
        }
        return null;
    }

    private String getToken(String name) {
        String value = null;
        HttpServletRequest request = getRequest();
        if (request != null) {
            String header = request.getHeader("authorization");
            if (header != null) {
                try {
                    value = Optional.ofNullable(MAPPER.readValue(DECODER.decode(header.split("\\.")[1]), Map.class).get(name))
                            .map(String::valueOf).orElse(null);
                } catch (IOException ignored) {
                }
            }
        }
        return value;
    }

    private HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        }
        return null;
    }

}
