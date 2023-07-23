package ir.baho.framework.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@MappedSuperclass
public abstract class BaseView<E extends BaseView<E, ID>, ID extends Serializable & Comparable<ID>> implements Entity<E, ID> {

    @Override
    public void setId(ID id) {
    }

    @Override
    public int compareTo(E o) {
        return this.getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseView<?, ?> that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return super.toString() + '@' + getId();
    }

}
