package ir.baho.framework.service;

import ir.baho.framework.dto.BaseIdDto;
import ir.baho.framework.dto.EntityMetadata;

import java.io.Serializable;

public interface BaseService<D extends BaseIdDto<D, ID>, ID extends Serializable & Comparable<ID>> {

    D create();

    D create(ID id);

    D save(D entity);

    D update(D entity);

    void delete(ID id);

    D find(ID id);

    EntityMetadata<ID> findMetadata(ID id);

}
