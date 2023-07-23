package ir.baho.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseDtoSimple<E extends BaseDtoSimple<E, ID>, ID extends Serializable & Comparable<ID>> extends BaseIdDto<E, ID> {

    private int version;

    private LocalDateTime lastModifiedDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseDtoSimple<?, ?> that)) return false;
        return getVersion() == that.getVersion() && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion());
    }

}
