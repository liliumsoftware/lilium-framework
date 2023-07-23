package ir.baho.framework.mapper;

import ir.baho.framework.domain.Entity;
import ir.baho.framework.dto.BaseDto;

public interface BaseMapper<E extends Entity<E, ?>, D extends BaseDto<D, ?>> extends BaseMapperSimple<E, D> {

    @IgnoreMetaData
    E toEntity(D dto);

}
