package ir.baho.framework.domain;

import java.io.Serializable;

public interface Entity<E extends Entity<E, ID>, ID extends Serializable & Comparable<ID>>
        extends Serializable, Comparable<E>, Cloneable {

    ID getId();

    void setId(ID id);

    @Override
    default int compareTo(E o) {
        return this.getId().compareTo(o.getId());
    }

}
