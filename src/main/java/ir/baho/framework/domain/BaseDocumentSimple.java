package ir.baho.framework.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseDocumentSimple<E extends BaseDocumentSimple<E, ID>, ID extends Serializable & Comparable<ID>>
        implements Entity<E, ID>, ir.baho.framework.domain.Version {

    @DiffIgnore
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @DiffIgnore
    @Version
    private int version;

    @Transient
    @SuppressWarnings("unchecked")
    public E id(ID id) {
        this.setId(id);
        return (E) this;
    }

    @Transient
    @SuppressWarnings("unchecked")
    public E clearId() {
        setId(null);
        return (E) this;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Transient
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
        if (!(o instanceof BaseDocumentSimple<?, ?> that)) return false;
        return getVersion() == that.getVersion() && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion());
    }

    @Override
    public String toString() {
        return super.toString() + '@' + getId();
    }

}
