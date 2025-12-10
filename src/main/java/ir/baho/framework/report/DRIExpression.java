package ir.baho.framework.report;

import java.io.Serializable;

public interface DRIExpression<T> extends Serializable {

    String getName();

    Class<? super T> getValueClass();

}
