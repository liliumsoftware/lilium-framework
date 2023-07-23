package ir.baho.framework.domain;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseDocument<E extends BaseDocument<E, ID>, ID extends Serializable & Comparable<ID>> extends BaseDocumentSimple<E, ID> {

    @DiffIgnore
    @CreatedBy
    @Size(max = 100)
    private String createdBy;

    @DiffIgnore
    @CreatedDate
    private LocalDateTime createdDate;

    @DiffIgnore
    @LastModifiedBy
    @Size(max = 100)
    private String lastModifiedBy;

}
