package ir.baho.framework.report;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

@Getter
public abstract class AbstractValueFormatter<T, U> implements DRIValueFormatter<T, U> {

    private final String name;

    protected AbstractValueFormatter() {
        this.name = ReportUtils.generateUniqueName("valueFormatter");
    }

    protected AbstractValueFormatter(String name) {
        Validate.notEmpty(name, "name must not be empty");
        this.name = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getValueClass() {
        return (Class<T>) ReportUtils.getGenericClass(this, 0);
    }

}
