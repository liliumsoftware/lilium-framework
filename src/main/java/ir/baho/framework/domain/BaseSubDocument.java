package ir.baho.framework.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;

import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class BaseSubDocument<E extends BaseSubDocument<E>> implements Entity<E, String> {

    @Override
    public void setId(String id) {
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
        if (!(o instanceof BaseSubDocument<?> that)) return false;
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
