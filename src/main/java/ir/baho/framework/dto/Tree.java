package ir.baho.framework.dto;

import java.io.Serializable;
import java.util.List;

public interface Tree<E extends Tree<E, ID>, ID extends Serializable & Comparable<ID>> {

    ID getId();

    ID getParentId();

    List<E> getChildren();

}
