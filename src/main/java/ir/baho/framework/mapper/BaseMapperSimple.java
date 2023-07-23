package ir.baho.framework.mapper;

import ir.baho.framework.domain.Entity;
import ir.baho.framework.dto.BaseDtoSimple;
import org.mapstruct.Mapping;

public interface BaseMapperSimple<E extends Entity<E, ?>, D extends BaseDtoSimple<D, ?>> extends BaseIdMapper<E, D> {

    @Mapping(target = "lastModifiedDate", ignore = true)
    E toEntity(D dto);

}
