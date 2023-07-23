package ir.baho.framework.domain;

import java.io.Serializable;

public interface IdProjection<ID extends Serializable & Comparable<ID>> {

    ID getId();

}
