package ir.baho.framework.metadata;

import ir.baho.framework.enumeration.EnumType;
import ir.baho.framework.time.CalendarType;
import ir.baho.framework.time.DurationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOptions implements Serializable {

    private String username;

    private Locale locale;

    private ZoneId zoneId;

    private CalendarType calendarType;

    private String dateFormat;

    private String dateTimeFormat;

    private String timeFormat;

    private DurationType durationType;

    private EnumType enumType;

    private List<String> roles;

    private List<String> groups;

    private boolean rtl;

}
