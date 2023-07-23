package ir.baho.framework.time;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Week {

    private int year;
    private int month;
    private int week;
    private LocalDate from;
    private LocalDate to;

}
