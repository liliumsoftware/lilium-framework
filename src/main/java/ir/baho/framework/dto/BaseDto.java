package ir.baho.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseDto<E extends BaseDto<E, ID>, ID extends Serializable & Comparable<ID>> extends BaseDtoSimple<E, ID> {

    private String createdBy;

    private LocalDateTime createdDate;

    private String lastModifiedBy;

}
