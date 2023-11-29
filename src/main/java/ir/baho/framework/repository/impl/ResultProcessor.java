package ir.baho.framework.repository.impl;

import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Window;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.util.ReactiveWrapperConverters;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ResultProcessor {

    private final ProjectingConverter converter;
    private final ReturnedType type;

    ResultProcessor(ProjectionFactory factory, ReturnedType type) {
        Assert.notNull(factory, "ProjectionFactory must not be null");
        Assert.notNull(type, "Type must not be null");

        this.type = type;
        this.converter = new ProjectingConverter(this.type, factory);
    }

    private static Collection<Object> createCollectionFor(Collection<?> source) {
        try {
            return CollectionFactory.createCollection(source.getClass(), source.size());
        } catch (RuntimeException o_O) {
            return CollectionFactory.createApproximateCollection(source, source.size());
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T processResult(@Nullable Object source, Converter<Object, Object> preparingConverter) {
        if (source == null || type.isInstance(source) || !type.isProjecting()) {
            return (T) source;
        }

        Assert.notNull(preparingConverter, "Preparing converter must not be null");

        ChainingConverter converter = ChainingConverter.of(type.getReturnedType(), preparingConverter).and(this.converter);

        if (source instanceof Window<?>) {
            return (T) ((Window<?>) source).map(converter::convert);
        }

        if (source instanceof Slice) {
            return (T) ((Slice<?>) source).map(converter::convert);
        }

        if (source instanceof Collection<?> collection) {
            Collection<Object> target = createCollectionFor(collection);

            for (Object columns : collection) {
                target.add(type.isInstance(columns) ? columns : converter.convert(columns));
            }

            return (T) target;
        }

        if (source instanceof Stream) {
            return (T) ((Stream<Object>) source).map(t -> type.isInstance(t) ? t : converter.convert(t));
        }

        if (ReactiveWrapperConverters.supports(source.getClass())) {
            return ReactiveWrapperConverters.map(source, it -> processResult(it, preparingConverter));
        }

        return (T) converter.convert(source);
    }

    private static class ChainingConverter implements Converter<Object, Object> {

        private final Class<?> targetType;
        private final Converter<Object, Object> delegate;

        private ChainingConverter(Class<?> targetType, Converter<Object, Object> delegate) {
            this.targetType = targetType;
            this.delegate = delegate;
        }

        public static ChainingConverter of(Class<?> targetType, Converter<Object, Object> delegate) {
            return new ChainingConverter(targetType, delegate);
        }

        public ChainingConverter and(final Converter<Object, Object> converter) {
            Assert.notNull(converter, "Converter must not be null");

            return new ChainingConverter(targetType, source -> {
                if (targetType.isInstance(source)) {
                    return source;
                }

                Object intermediate = ChainingConverter.this.convert(source);

                return intermediate == null || targetType.isInstance(intermediate) ? intermediate : converter.convert(intermediate);
            });
        }

        @Nullable
        @Override
        public Object convert(Object source) {
            return delegate.convert(source);
        }

    }

    private static class ProjectingConverter implements Converter<Object, Object> {

        private final ReturnedType type;
        private final ProjectionFactory factory;
        private final ConversionService conversionService;

        ProjectingConverter(ReturnedType type, ProjectionFactory factory) {
            this(type, factory, DefaultConversionService.getSharedInstance());
        }

        public ProjectingConverter(ReturnedType type, ProjectionFactory factory, ConversionService conversionService) {
            this.type = type;
            this.factory = factory;
            this.conversionService = conversionService;
        }

        private static Map<String, Object> toMap(Collection<?> values, List<String> names) {
            int i = 0;
            Map<String, Object> result = new HashMap<>(values.size());

            for (Object element : values) {
                result.put(names.get(i++), element);
            }

            return result;
        }

        @Nullable
        @Override
        public Object convert(Object source) {
            Class<?> targetType = type.getReturnedType();

            if (targetType.isInterface()) {
                return factory.createProjection(targetType, getProjectionTarget(source));
            }

            return conversionService.convert(source, targetType);
        }

        private Object getProjectionTarget(Object source) {
            if (source != null && source.getClass().isArray()) {
                source = Arrays.asList((Object[]) source);
            }

            if (source instanceof Collection) {
                return toMap((Collection<?>) source, type.getInputProperties());
            }

            return source;
        }

    }

}
