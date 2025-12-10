package ir.baho.framework.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.ProjectionInformation;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ReturnedType {

    private static final Log logger = LogFactory.getLog(ReturnedType.class);

    private static final Map<CacheKey, ReturnedType> cache = new ConcurrentReferenceHashMap<>(32);

    private final Class<?> domainType;

    protected ReturnedType(Class<?> domainType) {
        this.domainType = domainType;
    }

    public static ReturnedType of(Class<?> returnedType, Class<?> domainType, ProjectionFactory factory) {
        Assert.notNull(returnedType, "Returned type must not be null");
        Assert.notNull(domainType, "Domain type must not be null");
        Assert.notNull(factory, "ProjectionFactory must not be null");

        return cache.computeIfAbsent(CacheKey.of(returnedType, domainType, factory.hashCode()), _ -> returnedType.isInterface()
                ? new ReturnedInterface(factory.getProjectionInformation(returnedType), domainType)
                : new ReturnedClass(returnedType, domainType));
    }

    public final Class<?> getDomainType() {
        return domainType;
    }

    @Contract("null -> false")
    public final boolean isInstance(@Nullable Object source) {
        return getReturnedType().isInstance(source);
    }

    public abstract Class<?> getReturnedType();

    public abstract boolean isProjecting();

    public boolean isDtoProjection() {
        return isProjecting() && !getReturnedType().isInterface();
    }

    public abstract List<String> getInputProperties();

    public boolean hasInputProperties() {
        return !CollectionUtils.isEmpty(getInputProperties());
    }

    public abstract boolean needsCustomConstruction();

    private static final class ReturnedInterface extends ReturnedType {

        private final ProjectionInformation information;
        private final boolean isProjecting;
        private final List<String> inputProperties;

        public ReturnedInterface(ProjectionInformation information, Class<?> domainType) {
            super(domainType);

            Assert.notNull(information, "Projection information must not be null");

            this.information = information;
            this.isProjecting = !information.getType().isAssignableFrom(domainType);
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
            return isProjecting;
        }

        @Override
        public boolean isDtoProjection() {
            return false;
        }

        @Override
        public List<String> getInputProperties() {
            return inputProperties;
        }

        @Override
        public boolean needsCustomConstruction() {
            return isProjecting() && information.isClosed();
        }

    }

    private static final class ReturnedClass extends ReturnedType {

        private static final Set<Class<?>> VOID_TYPES = Set.of(Void.class, void.class);

        private final Class<?> type;
        private final boolean isDto;
        private final @Nullable PreferredConstructor<?, ?> constructor;
        private final Lazy<List<String>> inputProperties;

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

            this.constructor = detectConstructor(type);

            if (this.constructor == null) {
                this.inputProperties = Lazy.of(Collections.emptyList());
            } else {
                this.inputProperties = Lazy.of(this::detectConstructorParameterNames);
            }
        }

        @Override
        public Class<?> getReturnedType() {
            return type;
        }

        @Override
        public boolean isProjecting() {
            return isDto;
        }

        @Override
        public boolean isDtoProjection() {
            return isProjecting();
        }

        @Override
        public List<String> getInputProperties() {
            return inputProperties.get();
        }

        @Override
        public boolean hasInputProperties() {
            return this.constructor != null && this.constructor.getParameterCount() > 0 && super.hasInputProperties();
        }

        @Override
        public boolean needsCustomConstruction() {
            return isDtoProjection() && hasInputProperties();
        }

        private boolean isDomainSubtype() {
            return getDomainType().equals(type) && getDomainType().isAssignableFrom(type);
        }

        private boolean isPrimitiveOrWrapper() {
            return ClassUtils.isPrimitiveOrWrapper(type);
        }

        private @Nullable PreferredConstructor<?, ?> detectConstructor(Class<?> type) {
            return isDtoProjection() ? PreferredConstructorDiscoverer.discover(type) : null;
        }

        private List<String> detectConstructorParameterNames() {
            if (constructor == null) {
                return Collections.emptyList();
            }

            int parameterCount = constructor.getConstructor().getParameterCount();
            List<String> properties = new ArrayList<>(parameterCount);

            for (Parameter<Object, ?> parameter : constructor.getParameters()) {
                if (parameter.hasName()) {
                    properties.add(parameter.getRequiredName());
                }
            }

            if (properties.isEmpty() && parameterCount > 0) {
                if (logger.isWarnEnabled()) {
                    logger.warn(("No constructor parameter names discovered. "
                            + "Compile the affected code with '-parameters' instead or avoid its introspection: %s")
                            .formatted(constructor.getConstructor().getDeclaringClass().getName()));
                }
            }

            return Collections.unmodifiableList(properties);
        }

    }

    private record CacheKey(Class<?> returnedType, Class<?> domainType, int projectionFactoryHashCode) {

        public static CacheKey of(Class<?> returnedType, Class<?> domainType, int projectionFactoryHashCode) {
            return new CacheKey(returnedType, domainType, projectionFactoryHashCode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof CacheKey(Class<?> type, Class<?> dType, int factoryHashCode))) {
                return false;
            }

            if (projectionFactoryHashCode != factoryHashCode) {
                return false;
            }

            if (!ObjectUtils.nullSafeEquals(returnedType, type)) {
                return false;
            }

            return ObjectUtils.nullSafeEquals(domainType, dType);
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
            return "ReturnedType.CacheKey(returnedType=" + this.returnedType() + ", domainType=" + this.domainType()
                    + ", projectionFactoryHashCode=" + this.projectionFactoryHashCode() + ")";
        }

    }

}
