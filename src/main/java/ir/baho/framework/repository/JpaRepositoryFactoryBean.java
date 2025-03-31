package ir.baho.framework.repository;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.repository.impl.JpaRepositoryFactory;
import jakarta.persistence.EntityManager;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.util.List;

public class JpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean<T, S, ID> {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private List<StringConverter<?>> converters;

    @Setter
    private Class<?> revisionEntityClass;

    public JpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new JpaRepositoryFactory(applicationContext, entityManager, revisionEntityClass, messageResource, converters);
    }

}
