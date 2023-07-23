package ir.baho.framework.time;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface CalendarDay {

    LocalDate getDate();

    LocalTime getStartTime();

    LocalTime getEndTime();

    default LocalDateTime getStartDateTime() {
        return getDate().atTime(getStartTime());
    }

    default LocalDateTime getEndDateTime() {
        return getDate().atTime(getEndTime());
    }

    default Duration getDuration() {
        return Duration.between(getStartTime(), getEndTime());
    }

}
