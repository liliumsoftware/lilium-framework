package ir.baho.framework.dto;

import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.List;

public interface Tree<E extends Tree<E, ID>, ID extends Serializable & Comparable<ID>> {

    ID getId();

    ID getParentId();

    @NonNull
    List<E> getChildren();

}
