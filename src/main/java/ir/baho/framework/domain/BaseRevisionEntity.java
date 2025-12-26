package ir.baho.framework.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.RevisionTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder(toBuilder = true)
@MappedSuperclass
public abstract class BaseRevisionEntity<E extends BaseRevisionEntity<E, ID>, ID extends Number & Comparable<ID>> implements Serializable, Comparable<E> {

    @RevisionTimestamp
    private Date timestamp;

    private String username;

    public abstract ID getId();

    public abstract void setId(ID id);

    @Override
    public int compareTo(E o) {
        return this.getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseRevisionEntity<?, ?> that)) return false;
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
