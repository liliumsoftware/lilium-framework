package ir.baho.framework.repository.impl;

import lombok.Getter;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.ProjectionInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ReturnedType {

    private static final Map<CacheKey, ReturnedType> cache = new ConcurrentReferenceHashMap<>(32);

    private final Class<?> domainType;

    private ReturnedType(Class<?> domainType) {
        this.domainType = domainType;
    }

    public static ReturnedType of(Class<?> returnedType, Class<?> domainType, ProjectionFactory factory) {
        Assert.notNull(returnedType, "Returned type must not be null");
        Assert.notNull(domainType, "Domain type must not be null");
        Assert.notNull(factory, "ProjectionFactory must not be null");

        return cache.computeIfAbsent(CacheKey.of(returnedType, domainType, factory.hashCode()), key -> returnedType.isInterface()
                ? new ReturnedInterface(factory.getProjectionInformation(returnedType), domainType)
                : new ReturnedClass(returnedType, domainType));
    }

    public final Class<?> getDomainType() {
        return domainType;
    }

    public final boolean isInstance(@Nullable Object source) {
        return getReturnedType().isInstance(source);
    }

    public abstract boolean isProjecting();

    public abstract Class<?> getReturnedType();

    public abstract List<String> getInputProperties();

    private static final class ReturnedInterface extends ReturnedType {

        private final ProjectionInformation information;
        private final Class<?> domainType;
        private final List<String> inputProperties;

        public ReturnedInterface(ProjectionInformation information, Class<?> domainType) {
            super(domainType);

            Assert.notNull(information, "Projection information must not be null");

            this.information = information;
            this.domainType = domainType;
            this.inputProperties = detectInputProperties(information);
        }

        private static List<String> detectInputProperties(ProjectionInformation information) {
            List<String> properties = new ArrayList<>();

            for (PropertyDescriptor descriptor : information.getInputProperties()) {
                if (!properties.contains(descriptor.getName())) {
                    properties.add(descriptor.getName());
                }
            }

            return Collections.unmodifiableList(properties);
        }

        @Override
        public Class<?> getReturnedType() {
            return information.getType();
        }

        @Override
        public boolean isProjecting() {
            return !information.getType().isAssignableFrom(domainType);
        }

        @Override
        public List<String> getInputProperties() {
            return inputProperties;
        }

    }

    private static final class ReturnedClass extends ReturnedType {

        private static final Set<Class<?>> VOID_TYPES = new HashSet<>(Arrays.asList(Void.class, void.class));

        private final Class<?> type;
        private final boolean isDto;
        private final List<String> inputProperties;

        public ReturnedClass(Class<?> returnedType, Class<?> domainType) {

            super(domainType);

            Assert.notNull(returnedType, "Returned type must not be null");
            Assert.notNull(domainType, "Domain type must not be null");
            Assert.isTrue(!returnedType.isInterface(), "Returned type must not be an interface");

            this.type = returnedType;
            this.isDto = !Object.class.equals(type) &&
                    !type.isEnum() &&
                    !isDomainSubtype() &&
                    !isPrimitiveOrWrapper() &&
                    !Number.class.isAssignableFrom(type) &&
                    !VOID_TYPES.contains(type) &&
                    !type.getPackage().getName().startsWith("java.");

            this.inputProperties = detectConstructorParameterNames(returnedType);
        }

        @Override
        public Class<?> getReturnedType() {
            return type;
        }

        @Override
        public boolean isProjecting() {
            return isDto();
        }

        @Override
        public List<String> getInputProperties() {
            return inputProperties;
        }

        private List<String> detectConstructorParameterNames(Class<?> type) {
            if (!isDto()) {
                return Collections.emptyList();
            }

            PreferredConstructor<?, ?> constructor = PreferredConstructorDiscoverer.discover(type);

            if (constructor == null) {
                return Collections.emptyList();
            }

            List<String> properties = new ArrayList<>(constructor.getConstructor().getParameterCount());

            for (Parameter<Object, ?> parameter : constructor.getParameters()) {
                properties.add(parameter.getName());
            }

            return Collections.unmodifiableList(properties);
        }

        private boolean isDto() {
            return isDto;
        }

        private boolean isDomainSubtype() {
            return getDomainType().equals(type) && getDomainType().isAssignableFrom(type);
        }

        private boolean isPrimitiveOrWrapper() {
            return ClassUtils.isPrimitiveOrWrapper(type);
        }
    }

    @Getter
    private static final class CacheKey {

        private final Class<?> returnedType;
        private final Class<?> domainType;
        private final int projectionFactoryHashCode;

        private CacheKey(Class<?> returnedType, Class<?> domainType, int projectionFactoryHashCode) {
            this.returnedType = returnedType;
            this.domainType = domainType;
            this.projectionFactoryHashCode = projectionFactoryHashCode;
        }

        public static CacheKey of(Class<?> returnedType, Class<?> domainType, int projectionFactoryHashCode) {
            return new CacheKey(returnedType, domainType, projectionFactoryHashCode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof CacheKey cacheKey)) {
                return false;
            }

            if (projectionFactoryHashCode != cacheKey.projectionFactoryHashCode) {
                return false;
            }

            if (!ObjectUtils.nullSafeEquals(returnedType, cacheKey.returnedType)) {
                return false;
            }

            return ObjectUtils.nullSafeEquals(domainType, cacheKey.domainType);
        }

        @Override
        public int hashCode() {
            int result = ObjectUtils.nullSafeHashCode(returnedType);
            result = 31 * result + ObjectUtils.nullSafeHashCode(domainType);
            result = 31 * result + projectionFactoryHashCode;
            return result;
        }

        @Override
        public String toString() {
            return "ReturnedType.CacheKey(returnedType=" + this.getReturnedType() + ", domainType=" + this.getDomainType()
                    + ", projectionFactoryHashCode=" + this.getProjectionFactoryHashCode() + ")";
        }

    }

}
