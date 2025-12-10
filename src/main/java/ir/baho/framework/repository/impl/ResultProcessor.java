package ir.baho.framework.repository.impl;

import org.jspecify.annotations.Nullable;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.SearchResults;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Window;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.util.ReactiveWrapperConverters;
import org.springframework.lang.Contract;
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

    @Contract("null, _ -> null; !null, _ -> !null")
    @SuppressWarnings("unchecked")
    public <T> @Nullable T processResult(@Nullable Object source, Converter<Object, Object> preparingConverter) {
        if (source == null || type.isInstance(source) || !type.isProjecting()) {
            return (T) source;
        }

        Assert.notNull(preparingConverter, "Preparing converter must not be null");

        ChainingConverter converter = ChainingConverter.of(type.getReturnedType(), preparingConverter).and(this.converter);

        switch (source) {
            case Window<?> objects -> {
                return (T) objects.map(converter::convert);
            }
            case Slice<?> slice -> {
                return (T) slice.map(converter::convert);
            }
            case SearchResults<?> results -> {
                return (T) results.map(converter::convert);
            }
            case Collection<?> collection -> {
                Collection<Object> target = createCollectionFor(collection);

                for (Object columns : collection) {
                    target.add(type.isInstance(columns) ? columns : converter.convert(columns));
                }

                return (T) target;
            }
            case Stream<?> _ -> {
                return (T) ((Stream<Object>) source).map(t -> type.isInstance(t) ? t : converter.convert(t));
            }
            default -> {
            }
        }

        if (ReactiveWrapperConverters.supports(source.getClass())) {
            return ReactiveWrapperConverters.map(source, it -> processResult(it, preparingConverter));
        }

        return (T) converter.convert(source);
    }

    private record ChainingConverter(Class<?> targetType,
                                     Converter<Object, Object> delegate
    ) implements Converter<Object, Object> {

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

        @Override
        public @Nullable Object convert(Object source) {
            return delegate.convert(source);
        }

    }

    private record ProjectingConverter(ReturnedType type,
                                       ProjectionFactory factory,
                                       ConversionService conversionService
    ) implements Converter<Object, Object> {

        ProjectingConverter(ReturnedType type, ProjectionFactory factory) {
            this(type, factory, DefaultConversionService.getSharedInstance());
        }

        private static Map<String, Object> toMap(Collection<?> values, List<String> names) {
            int i = 0;
            Map<String, Object> result = new HashMap<>(values.size());

            for (Object element : values) {
                result.put(names.get(i++), element);
            }

            return result;
        }

        @Override
        public @Nullable Object convert(Object source) {
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
