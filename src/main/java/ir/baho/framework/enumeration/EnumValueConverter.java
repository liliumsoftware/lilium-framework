package ir.baho.framework.enumeration;

import ir.baho.framework.converter.EnumConverter;

import java.io.Serializable;

public abstract class EnumValueConverter<E extends Enum<E> & EnumValue<T>, T extends Serializable & Comparable<T>> extends EnumConverter<E> {

}
