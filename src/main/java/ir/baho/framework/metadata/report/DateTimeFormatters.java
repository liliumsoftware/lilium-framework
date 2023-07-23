package ir.baho.framework.metadata.report;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import ir.baho.framework.i18n.DateTimes;
import ir.baho.framework.i18n.Strings;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.definition.ReportParameters;
import org.springframework.context.i18n.LocaleContextHolder;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateTimeFormatters {

    private final Locale locale;
    private final ZoneId zoneId;
    private final DateFormat dateFormat;
    private final DateFormat dateTimeFormat;
    private final DateTimeFormatter timeFormat;
    private final DurationType durationType;

    public DateTimeFormatters(Locale locale, ZoneId zoneId, CalendarType calendarType,
                              String dateFormat, String dateTimeFormat, String timeFormat, DurationType durationType) {
        this.locale = locale == null ? Locale.getDefault() : locale;
        this.zoneId = zoneId == null ? ZoneId.systemDefault() : zoneId;
        this.dateFormat = new SimpleDateFormat(dateFormat == null ? "yyyy-MM-dd" : dateFormat,
                calendarType == CalendarType.PERSIAN ? DateTimes.FA_ULOCALE : DateTimes.EN_ULOCALE);
        this.dateFormat.setTimeZone(com.ibm.icu.util.TimeZone.getTimeZone(this.zoneId.getId()));
        this.dateTimeFormat = new SimpleDateFormat(dateTimeFormat == null ? "yyyy-MM-dd'T'HH:mm:ss" : dateTimeFormat,
                calendarType == CalendarType.PERSIAN ? DateTimes.FA_ULOCALE : DateTimes.EN_ULOCALE);
        this.dateTimeFormat.setTimeZone(com.ibm.icu.util.TimeZone.getTimeZone(this.zoneId.getId()));
        this.timeFormat = timeFormat == null ? DateTimeFormatter.ISO_TIME : DateTimeFormatter.ofPattern(timeFormat);
        this.durationType = durationType;
    }

    public AbstractSimpleExpression<Object> now() {
        return new AbstractSimpleExpression<>() {
            @Override
            public Object evaluate(ReportParameters reportParameters) {
                return Strings.getText(dateTimeFormat.format(Date.from(LocalDateTime.now()
                        .atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId).toInstant())), locale);
            }
        };
    }

    public AbstractValueFormatter<String, Object> dateFormatter() {
        return new AbstractValueFormatter<>() {
            @Override
            public String format(Object value, ReportParameters reportParameters) {
                if (value == null) {
                    return null;
                }
                LocalDate date = ((Timestamp) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return Strings.getText(dateFormat.format(Date.from((date).atStartOfDay().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toInstant())), reportParameters.getLocale());
            }
        };
    }

    public AbstractValueFormatter<String, Object> dateTimeFormatter() {
        return new AbstractValueFormatter<>() {
            @Override
            public String format(Object value, ReportParameters reportParameters) {
                if (value == null) {
                    return null;
                }
                LocalDateTime dateTime = LocalDateTime.ofInstant(((Timestamp) value).toInstant(), zoneId);
                return Strings.getText(dateTimeFormat.format(Date.from(dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId).toInstant())), locale);
            }
        };
    }

    public AbstractValueFormatter<String, Object> timeFormatter() {
        return new AbstractValueFormatter<>() {
            @Override
            public String format(Object value, ReportParameters reportParameters) {
                if (value == null) {
                    return null;
                }
                LocalTime time = ((Timestamp) value).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
                return Strings.getText(timeFormat.format(time), reportParameters.getLocale());
            }
        };
    }

    public AbstractValueFormatter<String, Object> durationFormatter() {
        return new AbstractValueFormatter<>() {
            @Override
            public String format(Object value, ReportParameters reportParameters) {
                if (value == null) {
                    return null;
                }
                String durationValue = String.valueOf(value);
                Duration duration = durationValue.startsWith("PT") ? Duration.parse(durationValue) : Duration.ofNanos(Long.parseLong(durationValue));
                String text;
                if (durationType == null) {
                    text = duration.toString();
                } else {
                    boolean negative = duration.isNegative();
                    if (negative) {
                        duration = duration.abs();
                    }
                    text = switch (durationType) {
                        case MILLIS -> String.valueOf(duration.toMillis());
                        case HOUR -> String.format("%2d", duration.toHours());
                        case MINUTE -> String.format("%2d", duration.toMinutes());
                        case SECOND -> String.format("%2d", duration.toSeconds());
                        case HOUR_MINUTE -> String.format("%02d:%02d",
                                duration.toHours(), duration.toMinutesPart());
                        case MINUTE_SECOND -> String.format("%02d:%02d",
                                duration.toMinutes(), duration.toSecondsPart());
                        default -> String.format("%02d:%02d:%02d",
                                duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
                    };
                    if (negative) {
                        text = '-' + text;
                    }
                }
                return Strings.getText(text, reportParameters.getLocale());
            }
        };
    }

}
