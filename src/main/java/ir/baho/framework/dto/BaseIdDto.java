package ir.baho.framework.dto;

import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseIdDto<E extends BaseIdDto<E, ID>, ID extends Serializable & Comparable<ID>>
        implements Serializable, Comparable<E>, Cloneable {

    @Null
    private ID id;

    @SuppressWarnings("unchecked")
    public E id(ID id) {
        this.setId(id);
        return (E) this;
    }

    @SuppressWarnings("unchecked")
    public E clearId() {
        setId(null);
        return (E) this;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public E copy() {
        return (E) clone();
    }

    @Override
    public int compareTo(E o) {
        return this.getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseIdDto<?, ?> that)) return false;
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
