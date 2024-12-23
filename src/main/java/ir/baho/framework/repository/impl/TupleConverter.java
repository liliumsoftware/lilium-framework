package ir.baho.framework.repository.impl;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import org.springframework.core.convert.converter.Converter;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TupleConverter implements Converter<Object, Object> {

    private final ReturnedType type;
    private final UnaryOperator<Tuple> tupleWrapper;

    public TupleConverter(ReturnedType type) {
        this(type, false);
    }

    public TupleConverter(ReturnedType type, boolean nativeQuery) {
        Assert.notNull(type, "Returned type must not be null");
        this.type = type;
        this.tupleWrapper = nativeQuery ? FallbackTupleWrapper::new : UnaryOperator.identity();
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

        return new TupleBackedMap(tupleWrapper.apply(tuple));
    }

    static class TupleBackedMap implements Map<String, Object> {

        private static final String UNMODIFIABLE_MESSAGE = "A TupleBackedMap cannot be modified";

        private final Tuple tuple;

        TupleBackedMap(Tuple tuple) {
            this.tuple = tuple;
        }

        @Override
        public int size() {
            return tuple.getElements().size();
        }

        @Override
        public boolean isEmpty() {
            return tuple.getElements().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            try {
                tuple.get((String) key);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public boolean containsValue(Object value) {
            return Arrays.asList(tuple.toArray()).contains(value);
        }

        @Override
        @Nullable
        public Object get(Object key) {
            if (!(key instanceof String)) {
                return null;
            }

            try {
                return tuple.get((String) key);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public Set<String> keySet() {
            return tuple.getElements().stream().map(TupleElement::getAlias).collect(Collectors.toSet());
        }

        @Override
        public Collection<Object> values() {
            return Arrays.asList(tuple.toArray());
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return tuple.getElements().stream()
                    .map(e -> new HashMap.SimpleEntry<String, Object>(e.getAlias(), tuple.get(e)))
                    .collect(Collectors.toSet());
        }

    }

    static class FallbackTupleWrapper implements Tuple {

        private final Tuple delegate;
        private final UnaryOperator<String> fallbackNameTransformer = JdbcUtils::convertPropertyNameToUnderscoreName;

        FallbackTupleWrapper(Tuple delegate) {
            this.delegate = delegate;
        }

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            return get(tupleElement.getAlias(), tupleElement.getJavaType());
        }

        @Override
        public <X> X get(String s, Class<X> type) {
            try {
                return delegate.get(s, type);
            } catch (IllegalArgumentException original) {
                try {
                    return delegate.get(fallbackNameTransformer.apply(s), type);
                } catch (IllegalArgumentException next) {
                    original.addSuppressed(next);
                    throw original;
                }
            }
        }

        @Override
        public Object get(String s) {
            try {
                return delegate.get(s);
            } catch (IllegalArgumentException original) {
                try {
                    return delegate.get(fallbackNameTransformer.apply(s));
                } catch (IllegalArgumentException next) {
                    original.addSuppressed(next);
                    throw original;
                }
            }
        }

        @Override
        public <X> X get(int i, Class<X> aClass) {
            return delegate.get(i, aClass);
        }

        @Override
        public Object get(int i) {
            return delegate.get(i);
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public List<TupleElement<?>> getElements() {
            return delegate.getElements();
        }

    }

}
