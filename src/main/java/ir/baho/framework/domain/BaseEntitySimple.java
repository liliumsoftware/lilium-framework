package ir.baho.framework.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.NotAudited;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntitySimple<E extends BaseEntitySimple<E, ID>, ID extends Serializable & Comparable<ID>>
        implements Entity<E, ID>, ir.baho.framework.domain.Version {

    @DiffIgnore
    @NotAudited
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE", nullable = false)
    private LocalDateTime lastModifiedDate;

    @DiffIgnore
    @Version
    @Column(name = "VERSION")
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

    @Id
    public abstract ID getId();

    public abstract void setId(ID id);

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntitySimple<?, ?> that)) return false;
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
