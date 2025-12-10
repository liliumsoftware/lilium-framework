package ir.baho.framework.repository.impl;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.util.TupleBackedMap;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TupleConverter implements Converter<Object, Object> {

    private final ReturnedType type;
    private final UnaryOperator<Tuple> tupleWrapper;
    private final boolean dtoProjection;
    private final @Nullable PreferredConstructor<?, ?> preferredConstructor;

    public TupleConverter(ReturnedType type) {
        this(type, false);
    }

    public TupleConverter(ReturnedType type, boolean nativeQuery) {
        Assert.notNull(type, "Returned type must not be null");
        this.type = type;
        this.tupleWrapper = nativeQuery ? TupleBackedMap::underscoreAware : UnaryOperator.identity();
        this.dtoProjection = type.isDtoProjection() && type.needsCustomConstruction();

        if (this.dtoProjection) {
            this.preferredConstructor = PreferredConstructorDiscoverer.discover(type.getReturnedType());
        } else {
            this.preferredConstructor = null;
        }
    }

    private static List<Class<?>> getArgumentTypes(Object[] ctorArgs) {
        List<Class<?>> argTypes = new ArrayList<>(ctorArgs.length);

        for (Object ctorArg : ctorArgs) {
            argTypes.add(ctorArg == null ? Void.class : ctorArg.getClass());
        }
        return argTypes;
    }

    public static boolean isConstructorCompatible(Constructor<?> constructor, List<Class<?>> argumentTypes) {
        if (constructor.getParameterCount() != argumentTypes.size()) {
            return false;
        }

        for (int i = 0; i < argumentTypes.size(); i++) {
            MethodParameter methodParameter = MethodParameter.forExecutable(constructor, i);
            Class<?> argumentType = argumentTypes.get(i);

            if (!areAssignmentCompatible(methodParameter.getParameterType(), argumentType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean areAssignmentCompatible(Class<?> to, Class<?> from) {
        if (from == Void.class && !to.isPrimitive()) {
            return true;
        }

        if (to.isPrimitive()) {

            if (to == Short.TYPE) {
                return from == Character.class || from == Byte.class;
            }

            if (to == Integer.TYPE) {
                return from == Short.class || from == Character.class || from == Byte.class;
            }

            if (to == Long.TYPE) {
                return from == Integer.class || from == Short.class || from == Character.class || from == Byte.class;
            }

            if (to == Double.TYPE) {
                return from == Float.class;
            }

            return ClassUtils.isAssignable(to, from);
        }

        return ClassUtils.isAssignable(to, from);
    }

    @Override
    public Object convert(Object source) {
        if (!(source instanceof Tuple tuple)) {
            return source;
        }

        List<TupleElement<?>> elements = tuple.getElements();

        if (elements.size() == 1) {
            Object value = tuple.get(elements.getFirst());

            if (type.getDomainType().isInstance(value) || type.isInstance(value) || value == null) {
                return value;
            }
        }

        if (dtoProjection) {
            Object[] ctorArgs = new Object[elements.size()];
            for (int i = 0; i < ctorArgs.length; i++) {
                ctorArgs[i] = tuple.get(i);
            }

            List<Class<?>> argTypes = getArgumentTypes(ctorArgs);

            if (preferredConstructor != null && isConstructorCompatible(preferredConstructor.getConstructor(), argTypes)) {
                return BeanUtils.instantiateClass(preferredConstructor.getConstructor(), ctorArgs);
            }

            return BeanUtils.instantiateClass(getFirstMatchingConstructor(ctorArgs, argTypes), ctorArgs);
        }

        return new TupleBackedMap(tupleWrapper.apply(tuple));
    }

    private Constructor<?> getFirstMatchingConstructor(Object[] ctorArgs, List<Class<?>> argTypes) {
        for (Constructor<?> ctor : type.getReturnedType().getDeclaredConstructors()) {
            if (ctor.getParameterCount() != ctorArgs.length) {
                continue;
            }

            if (isConstructorCompatible(ctor, argTypes)) {
                return ctor;
            }
        }

        throw new IllegalStateException(String.format(
                "Cannot find compatible constructor for DTO projection '%s' accepting '%s'", type.getReturnedType().getName(),
                argTypes.stream().map(Class::getName).collect(Collectors.joining(", "))));
    }

}
