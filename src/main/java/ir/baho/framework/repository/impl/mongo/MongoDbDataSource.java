package ir.baho.framework.repository.impl.mongo;

import com.mongodb.BasicDBObject;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class MongoDbDataSource implements JRDataSource {

    private final MongoDbQuery query;
    private ConvertUtilsBean convertUtilsBean;
    private BasicDBObject currentDbObject;
    private boolean hasIterator;
    private boolean hasCommandResult;
    private Iterator<?> resultsIterator;
    private Map<?, ?> currentResult;

    public MongoDbDataSource(MongoDbQuery query) {
        this.hasIterator = false;
        this.hasCommandResult = false;
        log.info("New MongoDB Data Source");
        this.query = query;
        if (!(this.hasIterator = (query.iterator != null))) {
            this.hasCommandResult = (query.commandResults != null);
            this.resultsIterator = query.commandResults.iterator();
        }
        this.initConverter();
    }

    public void initConverter() {
        this.convertUtilsBean = new ConvertUtilsBean();
        DoubleConverter doubleConverter = new DoubleConverter();
        FloatConverter floatConverter = new FloatConverter();
        IntegerConverter integerConverter = new IntegerConverter();
        LongConverter longConverter = new LongConverter();
        ShortConverter shortConverter = new ShortConverter();
        DateTimeConverter dateConverter = new DateConverter();
        dateConverter.setLocale(Locale.getDefault());
        DateFormat formatter = DateFormat.getDateTimeInstance(3, 3, Locale.getDefault());
        String pattern = ((SimpleDateFormat) formatter).toPattern();
        dateConverter.setPattern(pattern);
        this.convertUtilsBean.register(doubleConverter, Double.TYPE);
        this.convertUtilsBean.register(doubleConverter, Double.class);
        this.convertUtilsBean.register(floatConverter, Float.TYPE);
        this.convertUtilsBean.register(floatConverter, Float.class);
        this.convertUtilsBean.register(integerConverter, Integer.TYPE);
        this.convertUtilsBean.register(integerConverter, Integer.class);
        this.convertUtilsBean.register(longConverter, Long.TYPE);
        this.convertUtilsBean.register(longConverter, Long.class);
        this.convertUtilsBean.register(shortConverter, Short.TYPE);
        this.convertUtilsBean.register(shortConverter, Short.class);
        this.convertUtilsBean.register(dateConverter, Date.class);
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        try {
            String name = field.getDescription();
            if (name == null || name.isEmpty()) {
                name = field.getName();
            }
            if (name == null) {
                return null;
            }
            String[] ids = name.split("\\.");
            if (this.hasIterator) {
                Object result = this.getCursorValue(ids);
                return this.converter(field, result);
            }
            if (this.hasCommandResult) {
                Object result = this.getCommandResult(ids);
                return this.converter(field, result);
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JRException(e.getMessage());
        }
    }

    @Override
    public boolean next() {
        boolean next = false;
        if (this.hasIterator && (next = this.query.iterator.hasNext())) {
            this.currentDbObject = (BasicDBObject) this.query.iterator.next();
        } else if (this.hasCommandResult) {
            next = this.resultsIterator.hasNext();
            this.currentResult = null;
            if (next) {
                this.currentResult = (Map<?, ?>) this.resultsIterator.next();
            }
        }
        return next;
    }

    public Object converter(JRField field, Object value) {
        if (value == null) {
            return null;
        }
        Class<?> requiredClass = field.getValueClass();
        if (requiredClass.equals(value.getClass())) {
            return value;
        }
        if (requiredClass == Object.class) {
            return value;
        }
        log.debug("Converting value " + value + " with type " + value.getClass().getName() + " to " + requiredClass.getName() + " type");
        try {
            if (requiredClass == String.class) {
                return value.toString();
            }
            return this.convertUtilsBean.convert(value, requiredClass);
        } catch (Exception e) {
            String message = "Conversion error, field name: \"" + field.getName() + "\" requested type: \"" + field.getValueClassName() + "\" received type: \"" + value.getClass().getName() + "\" value: \"" + value + "\"";
            log.error(message);
            message = message.concat("\n").concat(e.getMessage());
            throw new ClassCastException(message);
        }
    }

    private Object getCommandResult(String[] ids) {
        Map<?, ?> currentMap = this.currentResult;
        int index = 0;
        while (index < ids.length) {
            boolean isLast = index == ids.length - 1;
            String id = ids[index];
            Object currentFieldObject = currentMap.get(id);
            if (currentFieldObject == null) {
                return null;
            }
            if (currentFieldObject instanceof Map) {
                if (isLast) {
                    return currentFieldObject;
                }
                currentMap = (Map<?, ?>) currentFieldObject;
                ++index;
            } else {
                if (isLast) {
                    return currentFieldObject;
                }
                return null;
            }
        }
        return null;
    }

    private Object getCursorValue(String[] ids) {
        BasicDBObject fieldObject = this.currentDbObject;
        int index = 0;
        while (index < ids.length) {
            boolean isLast = index == ids.length - 1;
            String id = ids[index];
            Object currentFieldObject = fieldObject.get(id);
            if (currentFieldObject == null) {
                return null;
            }
            if (currentFieldObject instanceof BasicDBObject) {
                if (isLast) {
                    return currentFieldObject;
                }
                fieldObject = (BasicDBObject) currentFieldObject;
                ++index;
            } else {
                if (isLast) {
                    return currentFieldObject;
                }
                return null;
            }
        }
        return null;
    }

}
