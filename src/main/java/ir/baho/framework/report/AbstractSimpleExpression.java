package ir.baho.framework.report;

import org.apache.commons.lang3.Validate;

public abstract class AbstractSimpleExpression<T> implements DRISimpleExpression<T> {

    private final String name;

    protected AbstractSimpleExpression() {
        this.name = ReportUtils.generateUniqueName("simpleExpression");
    }

    protected AbstractSimpleExpression(String name) {
        Validate.notEmpty(name, "name must not be empty");
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? super T> getValueClass() {
        return (Class<T>) ReportUtils.getGenericClass(this, 0);
    }

}
