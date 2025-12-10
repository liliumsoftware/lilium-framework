package ir.baho.framework.repository.impl;

import ir.baho.framework.audit.JaversRepository;
import ir.baho.framework.audit.impl.JaversRepositoryImpl;
import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.repository.MongoCriteriaRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;

import java.util.List;

public class MongoRepositoryFactory extends org.springframework.data.mongodb.repository.support.MongoRepositoryFactory {

    private final ApplicationContext applicationContext;
    private final MongoOperations mongoOperations;
    private final MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;
    private final List<StringConverter<?>> converters;

    public MongoRepositoryFactory(ApplicationContext applicationContext, MongoOperations mongoOperations,
                                  List<StringConverter<?>> converters) {
        super(mongoOperations);
        this.applicationContext = applicationContext;
        this.mongoOperations = mongoOperations;
        this.mappingContext = mongoOperations.getConverter().getMappingContext();
        this.converters = converters;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation information) {
        if (MongoCriteriaRepository.class.isAssignableFrom(information.getRepositoryInterface())) {
            MongoPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(information.getDomainType());
            MongoEntityInformation<?, ?> entityInformation = new MappingMongoEntityInformation<>(entity, information.getIdType());
            return getTargetRepositoryViaReflection(information, entityInformation, mongoOperations, converters);
        }
        return super.getTargetRepository(information);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (MongoCriteriaRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            return MongoCriteriaRepositoryImpl.class;
        }
        return super.getRepositoryBaseClass(metadata);
    }

    @Override
    public void setRepositoryBaseClass(Class<?> repositoryBaseClass) {
    }

    @Override
    protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata, MongoOperations operations) {
        if (JaversRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
            Object javersFragment = instantiateClass(JaversRepositoryImpl.class, applicationContext, getEntityInformation(metadata));
            return RepositoryComposition.RepositoryFragments.just(javersFragment).append(super.getRepositoryFragments(metadata, operations));
        }
        return super.getRepositoryFragments(metadata, operations);
    }

}
