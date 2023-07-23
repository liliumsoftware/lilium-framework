package ir.baho.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseFileDto<E extends BaseFileDto<E, ID>, ID extends Serializable & Comparable<ID>> extends BaseDtoSimple<E, ID> {

    private String name;

    private String type;

    private int size;

    private byte[] value;

}
