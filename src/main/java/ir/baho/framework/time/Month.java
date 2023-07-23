package ir.baho.framework.time;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Month {

    private int year;
    private int month;
    private String name;
    private LocalDate from;
    private LocalDate to;

}
