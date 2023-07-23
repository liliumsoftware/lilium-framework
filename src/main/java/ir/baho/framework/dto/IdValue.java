package ir.baho.framework.dto;

import java.io.Serializable;

public record IdValue<ID extends Serializable & Comparable<ID>, S extends Serializable & Comparable<S>>
        (ID id, S value) {

}
