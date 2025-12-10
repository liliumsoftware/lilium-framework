package ir.baho.framework.report;

import java.sql.Connection;
import java.util.Locale;

public interface ReportParameters {

    String CROSSTAB_ROW_COUNTER = "CROSSTAB_ROW_NUMBER";

    <T> T getValue(String name);

    <T> T getValue(DRIValue<T> value);

    <T> T getFieldValue(String name);

    <T> T getVariableValue(String name);

    <T> T getParameterValue(String name);

    Integer getPageNumber();

    Integer getColumnNumber();

    Integer getReportRowNumber();

    Integer getPageRowNumber();

    Integer getColumnRowNumber();

    Integer getCrosstabRowNumber();

    Integer getGroupCount(String groupName);

    Connection getConnection();

    Locale getLocale();

    String getMessage(String key);

    String getMessage(String key, Object[] arguments);

    ReportParameters getMasterParameters();

}
