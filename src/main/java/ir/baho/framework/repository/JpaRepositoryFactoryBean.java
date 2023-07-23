package ir.baho.framework.repository;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.repository.impl.JpaRepositoryFactory;
import ir.baho.framework.service.CurrentUser;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.util.List;

public class JpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean<T, S, ID> {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private MessageResource messageResource;

    @Autowired
    private List<StringConverter<?>> converters;

    private Class<?> revisionEntityClass;

    public JpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    public void setRevisionEntityClass(Class<?> revisionEntityClass) {
        this.revisionEntityClass = revisionEntityClass;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new JpaRepositoryFactory(applicationContext, entityManager, revisionEntityClass, currentUser, messageResource, converters);
    }

}
