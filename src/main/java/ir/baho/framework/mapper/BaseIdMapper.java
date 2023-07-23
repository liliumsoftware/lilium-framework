package ir.baho.framework.mapper;

import ir.baho.framework.domain.Entity;
import ir.baho.framework.dto.BaseIdDto;

import java.util.List;

public interface BaseIdMapper<E extends Entity<E, ?>, D extends BaseIdDto<D, ?>> extends ProjectionMapper<E, D> {

    E toEntity(D dto);

    List<E> toEntity(List<D> dto);

}
