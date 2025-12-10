package ir.baho.framework.report;

public interface DRISimpleExpression<T> extends DRIValue<T>, DRIExpression<T> {

    T evaluate(ReportParameters reportParameters);

}
