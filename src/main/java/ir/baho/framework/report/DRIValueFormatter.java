package ir.baho.framework.report;

import java.io.Serializable;

public interface DRIValueFormatter<T, U> extends Serializable {

    T format(U value, ReportParameters reportParameters);

    Class<T> getValueClass();

}
