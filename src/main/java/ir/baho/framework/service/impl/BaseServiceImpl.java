package ir.baho.framework.service.impl;

import ir.baho.framework.dto.BaseIdDto;
import ir.baho.framework.dto.EntityMetadata;
import ir.baho.framework.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.stream.Stream;

public abstract class BaseServiceImpl<S extends BaseService<D, ID>, D extends BaseIdDto<D, ID>, ID extends Serializable & Comparable<ID>> implements BaseService<D, ID> {

    @Autowired
    protected ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    protected S self() {
        return (S) applicationContext.getBean(Stream.of(getClass().getInterfaces())
                .filter(BaseService.class::isAssignableFrom).findAny()
                .orElseThrow(() -> new IllegalStateException("Not a BaseService implementation")));
    }

    @Override
    public D create(ID id) {
        if (id == null) {
            return null;
        }
        D e = create();
        e = e.id(id);
        return e;
    }

    @Override
    public D save(D entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public D update(D entity) {
        return save(entity);
    }

    @Override
    public void delete(ID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public D find(ID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityMetadata<ID> findMetadata(ID id) {
        throw new UnsupportedOperationException();
    }

}
