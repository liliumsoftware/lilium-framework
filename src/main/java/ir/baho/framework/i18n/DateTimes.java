package ir.baho.framework.i18n;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import ir.baho.framework.service.CurrentUser;
import ir.baho.framework.time.CalendarDay;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import ir.baho.framework.time.Month;
import ir.baho.framework.time.Quarter;
import ir.baho.framework.time.SixMonth;
import ir.baho.framework.time.Week;
import ir.baho.framework.time.Year;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@AutoConfiguration
@RequiredArgsConstructor
public class DateTimes {

    public static final ULocale FA_ULOCALE = new ULocale("fa_IR@calendar=persian;numbers=latn");
    public static final ULocale EN_ULOCALE = new ULocale("en_US");

    private final CurrentUser currentUser;

    public LocalDate of(int year, int month, int day) {
        return of(year, month, day, currentUser.calendarType());
    }

    public LocalDate of(int year, int month, int day, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = getPersian(year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            return toLocalDate(calendar);
        } else {
            return LocalDate.of(year, month, day);
        }
    }

    public LocalDateTime of(int year, int month, int day, int hour, int minute) {
        return of(year, month, day, hour, minute, 0);
    }

    public LocalDateTime of(int year, int month, int day, int hour, int minute, CalendarType calendarType) {
        return of(year, month, day, hour, minute, 0, calendarType);
    }

    public LocalDateTime of(int year, int month, int day, int hour, int minute, int second) {
        return of(year, month, day, hour, minute, second, currentUser.calendarType());
    }

    public LocalDateTime of(int year, int month, int day, int hour, int minute, int second, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = getPersian(year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            return toLocalDateTime(calendar);
        } else {
            return LocalDateTime.of(year, month, day, hour, minute, second);
        }
    }

    public LocalDateTime of(LocalDate date, LocalTime time) {
        return of(date, time, LocaleContextHolder.getTimeZone().toZoneId());
    }

    public LocalDateTime of(LocalDate date, LocalTime time, ZoneId zoneId) {
        return date.atTime(time).atZone(zoneId)
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public LocalDateTime of(LocalDateTime dateTime, LocalTime time) {
        return of(dateTime, time, LocaleContextHolder.getTimeZone().toZoneId());
    }

    public LocalDateTime of(LocalDateTime dateTime, LocalTime time, ZoneId zoneId) {
        return dateTime.atZone(zoneId).with(time)
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public LocalDate getStart(int year) {
        return getStart(year, currentUser.calendarType());
    }

    public LocalDate getStart(int year, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = getPersian(year);
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return toLocalDate(calendar);
        } else {
            return LocalDate.of(year, 1, 1);
        }
    }

    public LocalDate getEnd(int year) {
        return getEnd(year, currentUser.calendarType());
    }

    public LocalDate getEnd(int year, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = getPersian(year);
            calendar.set(Calendar.MONTH, 11);
            calendar.set(Calendar.DAY_OF_MONTH, isLeapYear(year, calendarType) ? 30 : 29);
            return toLocalDate(calendar);
        } else {
            return LocalDate.of(year, 12, 31);
        }
    }

    public LocalDate getStart(int year, SixMonth sixMonth) {
        return getStart(year, sixMonth, currentUser.calendarType());
    }

    public LocalDate getStart(int year, SixMonth sixMonth, CalendarType calendarType) {
        if (sixMonth == SixMonth.FIRST) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 1, 1);
            }
        } else {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 6);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 7, 1);
            }
        }
    }

    public LocalDate getEnd(int year, SixMonth sixMonth) {
        return getEnd(year, sixMonth, currentUser.calendarType());
    }

    public LocalDate getEnd(int year, SixMonth sixMonth, CalendarType calendarType) {
        if (sixMonth == SixMonth.FIRST) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 5);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 6, 30);
            }
        } else {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 11);
                calendar.set(Calendar.DAY_OF_MONTH, isLeapYear(year, calendarType) ? 30 : 29);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 12, 31);
            }
        }
    }

    public LocalDate getStart(int year, Quarter quarter) {
        return getStart(year, quarter, currentUser.calendarType());
    }

    public LocalDate getStart(int year, Quarter quarter, CalendarType calendarType) {
        if (quarter == Quarter.FIRST) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 1, 1);
            }
        } else if (quarter == Quarter.SECOND) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 3);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 4, 1);
            }
        } else if (quarter == Quarter.THIRD) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 6);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 7, 1);
            }
        } else {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 9);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 10, 1);
            }
        }
    }

    public LocalDate getEnd(int year, Quarter quarter) {
        return getEnd(year, quarter, currentUser.calendarType());
    }

    public LocalDate getEnd(int year, Quarter quarter, CalendarType calendarType) {
        if (quarter == Quarter.FIRST) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 2);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 3, 31);
            }
        } else if (quarter == Quarter.SECOND) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 5);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 6, 30);
            }
        } else if (quarter == Quarter.THIRD) {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 8);
                calendar.set(Calendar.DAY_OF_MONTH, 30);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 9, 30);
            }
        } else {
            if (isPersian(calendarType)) {
                Calendar calendar = getPersian(year);
                calendar.set(Calendar.MONTH, 11);
                calendar.set(Calendar.DAY_OF_MONTH, isLeapYear(year, calendarType) ? 30 : 29);
                return toLocalDate(calendar);
            } else {
                return LocalDate.of(year, 12, 31);
            }
        }
    }

    public LocalDate getStart(int year, int month) {
        return getStart(year, month, currentUser.calendarType());
    }

    public LocalDate getStart(int year, int month, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = getPersian(year);
            calendar.set(Calendar.MONTH, month + 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            return toLocalDate(calendar);
        } else {
            return LocalDate.of(year, month, 1);
        }
    }

    public LocalDate getEnd(int year, int month) {
        return getEnd(year, month, currentUser.calendarType());
    }

    public LocalDate getEnd(int year, int month, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = getPersian(year);
            calendar.set(Calendar.MONTH, month + 1);
            switch (month) {
                case 1, 2, 3, 4, 5, 6 -> calendar.set(Calendar.DAY_OF_MONTH, 31);
                case 7, 8, 9, 10, 11 -> calendar.set(Calendar.DAY_OF_MONTH, 30);
                default -> {
                    if (isLeapYear(year, calendarType)) {
                        calendar.set(Calendar.DAY_OF_MONTH, 30);
                    } else {
                        calendar.set(Calendar.DAY_OF_MONTH, 29);
                    }
                }
            }
            return toLocalDate(calendar);
        } else {
            return switch (month) {
                case 1, 3, 5, 7, 8, 10, 12 -> LocalDate.of(year, month, 31);
                case 4, 6, 9, 11 -> LocalDate.of(year, month, 30);
                default ->
                        isLeapYear(year, calendarType) ? LocalDate.of(year, month, 29) : LocalDate.of(year, month, 28);
            };
        }
    }

    public List<Week> getWeeksBetween(LocalDate startDate, LocalDate endDate) {
        return getWeeksBetween(startDate, endDate, currentUser.calendarType());
    }

    public List<Week> getWeeksBetween(LocalDate startDate, LocalDate endDate, CalendarType calendarType) {
        List<Week> weeks = new ArrayList<>();
        if (isPersian(calendarType)) {
            Calendar start = toCalendar(startDate);
            start.set(Calendar.DAY_OF_WEEK, 0);

            Calendar end = toCalendar(endDate);
            end.set(Calendar.DAY_OF_WEEK, 6);

            int i = 0;
            while (true) {
                Week week = new Week();
                Calendar calendar = copy(start);
                calendar.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) + (i * 7));

                if (calendar.after(end)) {
                    break;
                }

                week.setFrom(toLocalDate(calendar));
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 6);
                week.setTo(toLocalDate(calendar));
                week.setYear(calendar.get(Calendar.YEAR));
                week.setMonth(calendar.get(Calendar.MONTH) + 1);
                week.setWeek(calendar.get(Calendar.WEEK_OF_YEAR));
                weeks.add(week);
                i++;
            }
        } else {
            LocalDate start = startDate.with(DayOfWeek.MONDAY);
            LocalDate end = endDate.with(DayOfWeek.SUNDAY);

            int i = 0;
            while (true) {
                Week week = new Week();
                LocalDate date = start.plusDays(i * 7L);

                if (date.isAfter(end)) {
                    break;
                }

                week.setFrom(date);
                week.setTo(date.plusDays(6));
                week.setYear(date.getYear());
                week.setMonth(date.getMonthValue());
                week.setWeek(date.get(ChronoField.ALIGNED_WEEK_OF_YEAR));
                weeks.add(week);
                i++;
            }
        }
        return weeks;
    }

    public List<Month> getMonthsBetween(LocalDate startDate, LocalDate endDate) {
        return getMonthsBetween(startDate, endDate, currentUser.calendarType());
    }

    public List<Month> getMonthsBetween(LocalDate startDate, LocalDate endDate, CalendarType calendarType) {
        List<Month> months = new ArrayList<>();
        if (isPersian(calendarType)) {
            Calendar start = toCalendar(startDate);
            start.set(Calendar.DAY_OF_MONTH, 1);

            Calendar end = toCalendar(endDate);
            end.set(Calendar.DAY_OF_MONTH, getLastDayOfMonth(end.get(Calendar.YEAR), end.get(Calendar.MONTH) + 1, calendarType));

            int i = 0;
            DateFormat format = new SimpleDateFormat("MMM", FA_ULOCALE);
            while (true) {
                Month month = new Month();
                Calendar calendar = copy(start);
                calendar.set(Calendar.MONTH, start.get(Calendar.MONTH) + i++);

                if (calendar.after(end)) {
                    break;
                }

                month.setFrom(toLocalDate(calendar));
                calendar.set(Calendar.DAY_OF_MONTH, getLastDayOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendarType));
                month.setYear(calendar.get(Calendar.YEAR));
                month.setTo(toLocalDate(calendar));
                month.setMonth(calendar.get(Calendar.MONTH) + 1);
                month.setName(format.format(calendar));
                months.add(month);
            }
        } else {
            LocalDate start = startDate.withDayOfMonth(1);
            LocalDate end = endDate.withDayOfMonth(getLastDayOfMonth(endDate.getYear(), endDate.getMonthValue(), calendarType));

            int i = 0;
            while (true) {
                Month month = new Month();
                LocalDate date = start.plusMonths(i++);

                if (date.isAfter(end)) {
                    break;
                }

                month.setFrom(date);
                month.setYear(date.getYear());
                month.setTo(date.plusDays(getLastDayOfMonth(date.getYear(), date.getMonthValue(), calendarType) - 1));
                month.setMonth(date.getYear());
                month.setName(date.getMonth().name());
                months.add(month);
            }
        }
        return months;
    }

    public List<Year> getYearsBetween(LocalDate startDate, LocalDate endDate) {
        return getYearsBetween(startDate, endDate, currentUser.calendarType());
    }

    public List<Year> getYearsBetween(LocalDate startDate, LocalDate endDate, CalendarType calendarType) {
        List<Year> years = new ArrayList<>();
        if (isPersian(calendarType)) {
            Calendar start = toCalendar(startDate);
            start.set(Calendar.MONTH, 0);
            start.set(Calendar.DAY_OF_MONTH, 1);

            Calendar end = toCalendar(endDate);
            end.set(Calendar.MONTH, 11);
            end.set(Calendar.DAY_OF_MONTH, getLastDayOfMonth(end.get(Calendar.YEAR), end.get(Calendar.MONTH) + 1, calendarType));

            int i = 0;
            while (true) {
                Year year = new Year();
                Calendar calendar = copy(start);
                calendar.set(Calendar.YEAR, start.get(Calendar.YEAR) + i++);

                if (calendar.after(end)) {
                    break;
                }

                year.setFrom(toLocalDate(calendar));
                calendar.set(Calendar.MONTH, 11);
                calendar.set(Calendar.DAY_OF_MONTH, getLastDayOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendarType));
                year.setTo(toLocalDate(calendar));
                year.setYear(calendar.get(Calendar.YEAR));
                years.add(year);
            }
        } else {
            LocalDate start = startDate.withDayOfYear(1);
            LocalDate end = endDate.withDayOfYear(isLeapYear(endDate.getYear(), calendarType) ? 366 : 365);

            int i = 0;
            while (true) {
                Year year = new Year();
                LocalDate date = start.plusYears(i++);

                if (date.isAfter(end)) {
                    break;
                }

                year.setFrom(date);
                year.setTo(date.withDayOfYear(isLeapYear(endDate.getYear(), calendarType) ? 366 : 365));
                year.setYear(date.getYear());
                years.add(year);
            }
        }
        return years;
    }

    public int getYear(LocalDate date) {
        return getYear(date, currentUser.calendarType());
    }

    public int getYear(LocalDate date, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = toCalendar(date);
            return calendar.get(Calendar.YEAR);
        } else {
            return date.getYear();
        }
    }

    public int getMonth(LocalDate date) {
        return getMonth(date, currentUser.calendarType());
    }

    public int getMonth(LocalDate date, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = toCalendar(date);
            return calendar.get(Calendar.MONTH) + 1;
        } else {
            return date.getMonthValue();
        }
    }

    public int getDay(LocalDate date) {
        return getDay(date, currentUser.calendarType());
    }

    public int getDay(LocalDate date, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            Calendar calendar = toCalendar(date);
            return calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            return date.getDayOfMonth();
        }
    }

    public SixMonth getSixMonth(LocalDate date) {
        return getSixMonth(date, currentUser.calendarType());
    }

    public SixMonth getSixMonth(LocalDate date, CalendarType calendarType) {
        return getMonth(date, calendarType) < 7 ? SixMonth.FIRST : SixMonth.SECOND;
    }

    public Quarter getQuarter(LocalDate date) {
        return getQuarter(date, currentUser.calendarType());
    }

    public Quarter getQuarter(LocalDate date, CalendarType calendarType) {
        return switch (getMonth(date, calendarType)) {
            case 1, 2, 3 -> Quarter.FIRST;
            case 4, 5, 6 -> Quarter.SECOND;
            case 7, 8, 9 -> Quarter.THIRD;
            default -> Quarter.FORTH;
        };
    }

    public int getLastDayOfMonth(int year, int month) {
        return getLastDayOfMonth(year, month, currentUser.calendarType());
    }

    public int getLastDayOfMonth(int year, int month, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            return switch (month) {
                case 1, 2, 3, 4, 5, 6 -> 31;
                case 7, 8, 9, 10, 11 -> 30;
                default -> isLeapYear(year, calendarType) ? 30 : 29;
            };
        } else {
            return switch (month) {
                case 1, 3, 5, 7, 8, 10, 12 -> 31;
                case 4, 6, 9, 11 -> 30;
                default -> isLeapYear(year, calendarType) ? 29 : 28;
            };
        }
    }

    public boolean isLeapYear(int year) {
        return isLeapYear(year, currentUser.calendarType());
    }

    public boolean isLeapYear(int year, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            return ((year % 33 == 1) || (year % 33 == 5) || (year % 33 == 9)
                    || (year % 33 == 13) || (year % 33 == 17) || (year % 33 == 22)
                    || (year % 33 == 26) || (year % 33 == 30));
        } else {
            return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
        }
    }

    public LocalDate parseDate(String date, String pattern) {
        return parseDate(date, pattern, currentUser.calendarType());
    }

    @SneakyThrows
    public LocalDate parseDate(String date, String pattern, CalendarType calendarType) {
        DateFormat dateFormat = getDateFormat(pattern == null ? "yyyy-MM-dd" : pattern, calendarType);
        Instant instant = dateFormat.parse(date).toInstant();
        return LocalDateTime.ofInstant(instant, LocaleContextHolder.getTimeZone().toZoneId()).toLocalDate();
    }

    public LocalDateTime parseDateTime(String date, String pattern) {
        return parseDateTime(date, pattern, currentUser.calendarType());
    }

    @SneakyThrows
    public LocalDateTime parseDateTime(String date, String pattern, CalendarType calendarType) {
        DateFormat dateFormat = getDateFormat(pattern == null ? "yyyy-MM-dd'T'HH:mm:ss" : pattern, calendarType);
        return ZonedDateTime.ofInstant(dateFormat.parse(date).toInstant(), LocaleContextHolder.getTimeZone().toZoneId()).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public LocalTime parseTime(String time, String pattern) {
        DateTimeFormatter dateTimeFormatter = pattern == null ? DateTimeFormatter.ISO_TIME : DateTimeFormatter.ofPattern(pattern);
        return LocalTime.parse(time, dateTimeFormatter);
    }

    public Duration parseDuration(String duration, DurationType durationType) {
        if (durationType == null) {
            return Duration.parse(duration);
        }
        boolean negative = false;
        if (duration.startsWith("-")) {
            duration = duration.substring(1);
            negative = true;
        }
        String[] parts = duration.split(":");
        Duration d = switch (durationType) {
            case MILLIS -> Duration.ofMillis((Integer.parseInt(duration)));
            case HOUR -> Duration.ofHours((Integer.parseInt(parts[0])));
            case MINUTE -> Duration.ofMinutes((Integer.parseInt(parts[0])));
            case SECOND -> Duration.ofSeconds((Integer.parseInt(parts[0])));
            case HOUR_MINUTE -> Duration.ofSeconds((Integer.parseInt(parts[0]) * 3600L)
                    + (Integer.parseInt(parts[1]) * 60L));
            case MINUTE_SECOND -> Duration.ofSeconds((Integer.parseInt(parts[0]) * 60L) + (Integer.parseInt(parts[1])));
            default -> Duration.ofSeconds((Integer.parseInt(parts[0]) * 3600L)
                    + (Integer.parseInt(parts[1]) * 60L) + (Integer.parseInt(parts[2])));
        };
        return negative ? d.negated() : d;
    }

    public String format(LocalDate localDate, String pattern) {
        return format(localDate, pattern, currentUser.calendarType());
    }

    public String format(LocalDate localDate, String pattern, CalendarType calendarType) {
        DateFormat dateFormat = getDateFormat(pattern == null ? "yyyy-MM-dd" : pattern, calendarType);
        Date date = Date.from(localDate.atStartOfDay().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toInstant());
        return dateFormat.format(date);
    }

    public String format(LocalDateTime localDateTime, String pattern) {
        return format(localDateTime, pattern, currentUser.calendarType());
    }

    public String format(LocalDateTime localDateTime, String pattern, CalendarType calendarType) {
        DateFormat dateFormat = getDateFormat(pattern == null ? "yyyy-MM-dd'T'HH:mm:ss" : pattern, calendarType);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(LocaleContextHolder.getTimeZone().toZoneId()).toInstant());
        return dateFormat.format(date);
    }

    public String format(LocalTime localTime, String pattern) {
        DateTimeFormatter dateTimeFormatter = pattern == null ? DateTimeFormatter.ISO_TIME : DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(localTime);
    }

    public String format(Duration duration, DurationType durationType) {
        if (durationType == null) {
            return duration.toString();
        }
        boolean negative = duration.isNegative();
        if (negative) {
            duration = duration.abs();
        }
        String text = switch (durationType) {
            case MILLIS -> String.valueOf(duration.toMillis());
            case HOUR -> String.format("%2d", duration.toHours());
            case MINUTE -> String.format("%2d", duration.toMinutes());
            case SECOND -> String.format("%2d", duration.toSeconds());
            case HOUR_MINUTE -> String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart());
            case MINUTE_SECOND -> String.format("%02d:%02d", duration.toMinutes(), duration.toSecondsPart());
            default -> String.format("%02d:%02d:%02d", duration.toHours(),
                    duration.toMinutesPart(), duration.toSecondsPart());
        };
        if (negative) {
            text = '-' + text;
        }
        return text;
    }

    public DateFormat getDateFormat(String pattern) {
        return getDateFormat(pattern, currentUser.calendarType());
    }

    public DateFormat getDateFormat(String pattern, CalendarType calendarType) {
        if (isPersian(calendarType)) {
            return getDateFormat(pattern, DateTimes.FA_ULOCALE);
        } else {
            return getDateFormat(pattern, DateTimes.EN_ULOCALE);
        }
    }

    public DateFormat getDateFormat(String pattern, ULocale locale) {
        DateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone(LocaleContextHolder.getTimeZone().toZoneId().getId()));
        return dateFormat;
    }

    public LocalDateTime atStartOfDay(LocalDateTime dateTime) {
        return dateTime.with(ChronoField.NANO_OF_DAY, 0).atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public LocalDateTime atEndOfDay(LocalDateTime dateTime) {
        return dateTime.with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay()).atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public LocalDateTime atStartOfDay(LocalDate date) {
        return date.atStartOfDay(LocaleContextHolder.getTimeZone().toZoneId())
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public LocalDateTime atEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX).atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public <C extends CalendarDay> LocalDateTime plus(LocalDateTime start, Duration duration, List<C> days) {
        for (CalendarDay day : days) {
            Duration startDuration;
            if (day.getDuration().equals(Duration.ZERO) || start.isAfter(day.getEndDateTime()) || start.equals(day.getEndDateTime())) {
                continue;
            } else if (start.isBefore(day.getStartDateTime())) {
                startDuration = Stream.of(day.getDuration(), duration).min(Comparator.naturalOrder()).get();
                start = day.getStartDateTime().plus(startDuration);
            } else {
                startDuration = Stream.of(Duration.between(start, day.getEndDateTime()), duration).min(Comparator.naturalOrder()).get();
                start = start.plus(startDuration);
            }
            duration = duration.minus(startDuration);
            if (duration.isZero()) {
                break;
            }
        }
        return start;
    }

    public <C extends CalendarDay> LocalDateTime getStart(LocalDateTime dateTime, List<C> days) {
        for (CalendarDay day : days) {
            if (!day.getDuration().equals(Duration.ZERO) && !dateTime.isAfter(day.getEndDateTime()) && !dateTime.equals(day.getEndDateTime())) {
                if (dateTime.isBefore(day.getStartDateTime())) {
                    return day.getStartDateTime();
                } else {
                    return dateTime;
                }
            }
        }
        return dateTime;
    }

    public <C extends CalendarDay> Duration getDuration(LocalDateTime start, LocalDateTime end, List<C> days) {
        Duration duration = Duration.ZERO;
        for (CalendarDay day : days) {
            if (start.toLocalDate().equals(day.getDate()) && !start.isBefore(day.getStartDateTime())) {
                if (start.isBefore(day.getEndDateTime()) || start.equals(day.getEndDateTime())) {
                    duration = duration.plus(Duration.between(start.toLocalTime(), day.getEndTime()));
                }
            } else if (end.toLocalDate().equals(day.getDate()) && end.isBefore(day.getEndDateTime())) {
                if (end.isAfter(day.getStartDateTime())) {
                    duration = duration.plus(Duration.between(day.getStartTime(), end.toLocalTime()));
                }
            } else {
                duration = duration.plus(day.getDuration());
            }
        }
        return duration;
    }

    private Calendar toCalendar(LocalDate date) {
        Calendar calendar = Calendar.getInstance(EN_ULOCALE);
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());

        Calendar persian = Calendar.getInstance(FA_ULOCALE);
        persian.setTimeInMillis(calendar.getTimeInMillis());
        return persian;
    }

    private LocalDate toLocalDate(Calendar calendar) {
        return calendar.getTime().toInstant().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Calendar calendar) {
        return calendar.getTime().toInstant().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDateTime();
    }

    private Calendar copy(Calendar calendar) {
        Calendar copy = Calendar.getInstance(FA_ULOCALE);
        copy.setTimeInMillis(calendar.getTimeInMillis());
        return copy;
    }

    private Calendar getPersian(int year) {
        Calendar calendar = Calendar.getInstance(FA_ULOCALE);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private boolean isPersian(CalendarType calendarType) {
        return calendarType == CalendarType.PERSIAN;
    }

}
