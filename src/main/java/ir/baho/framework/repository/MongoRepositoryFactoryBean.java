package ir.baho.framework.repository;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.repository.impl.MongoRepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;
import java.util.List;

public class MongoRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean<T, S, ID> {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private List<StringConverter<?>> converters;

    public MongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
        return new MongoRepositoryFactory(applicationContext, operations, messageResource, converters);
    }

}
