package ir.baho.framework.time;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Year {

    private int year;
    private LocalDate from;
    private LocalDate to;

}
