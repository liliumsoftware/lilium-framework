package ir.baho.framework.repository.impl;

import ir.baho.framework.audit.EnversRepository;
import ir.baho.framework.audit.JaversRepository;
import ir.baho.framework.audit.impl.EnversRepositoryImpl;
import ir.baho.framework.audit.impl.JaversRepositoryImpl;
import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.repository.JpaCriteriaRepository;
import ir.baho.framework.service.CurrentUser;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.context.ApplicationContext;
import org.springframework.data.envers.repository.support.ReflectionRevisionEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.history.support.RevisionEntityInformation;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public class JpaRepositoryFactory extends org.springframework.data.jpa.repository.support.JpaRepositoryFactory {

    private final ApplicationContext applicationContext;
    private final EntityManager entityManager;
    private final RevisionEntityInformation revisionEntityInformation;
    private final CurrentUser currentUser;
    private final MessageResource messageResource;
    private final List<StringConverter<?>> converters;

    public JpaRepositoryFactory(ApplicationContext applicationContext, EntityManager entityManager, Class<?> revisionEntityClass,
                                CurrentUser currentUser, MessageResource messageResource,
                                List<StringConverter<?>> converters) {
        super(entityManager);
        this.applicationContext = applicationContext;
        this.entityManager = entityManager;
        if (revisionEntityClass != null) {
            this.revisionEntityInformation = Optional.of(revisionEntityClass)
                    .filter(it -> !it.equals(DefaultRevisionEntity.class))
                    .<RevisionEntityInformation>map(ReflectionRevisionEntityInformation::new)
                    .orElseGet(DefaultRevisionEntityInformation::new);
        } else {
            this.revisionEntityInformation = new DefaultRevisionEntityInformation();
        }
        this.currentUser = currentUser;
        this.messageResource = messageResource;
        this.converters = converters;
    }

    @Override
    protected JpaRepositoryImplementation<?, ?> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
        if (JpaCriteriaRepository.class.isAssignableFrom(information.getRepositoryInterface())) {
            JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
            return getTargetRepositoryViaReflection(information, entityInformation, entityManager, currentUser, messageResource, converters);
        }
        return super.getTargetRepository(information, entityManager);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (JpaCriteriaRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            return JpaCriteriaRepositoryImpl.class;
        }
        return super.getRepositoryBaseClass(metadata);
    }

    @Override
    protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata) {
        if (JaversRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            Object javersFragment = instantiateClass(JaversRepositoryImpl.class,
                    applicationContext, getEntityInformation(metadata.getDomainType()), currentUser);
            return RepositoryComposition.RepositoryFragments.just(javersFragment).append(super.getRepositoryFragments(metadata));
        } else if (EnversRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            Object enversFragment = instantiateClass(EnversRepositoryImpl.class,
                    getEntityInformation(metadata.getDomainType()), revisionEntityInformation, entityManager, currentUser, converters);
            return RepositoryComposition.RepositoryFragments.just(enversFragment).append(super.getRepositoryFragments(metadata));
        }
        return super.getRepositoryFragments(metadata);
    }

}
