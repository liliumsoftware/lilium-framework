package ir.baho.framework.enumeration;

import jakarta.persistence.AttributeConverter;

import java.io.Serializable;
import java.util.EnumSet;

public abstract class EnumValueAttributeConverter<E extends Enum<E> & EnumValue<T>, T extends Serializable & Comparable<T>>
        extends EnumValueConverter<E, T> implements AttributeConverter<E, T> {

    @Override
    public T convertToDatabaseColumn(E enumValue) {
        return enumValue != null ? enumValue.getValue() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E convertToEntityAttribute(T value) {
        return EnumSet.allOf((Class<E>) getType()).stream().filter(e -> e.getValue().equals(value)).findAny().orElse(null);
    }

}
