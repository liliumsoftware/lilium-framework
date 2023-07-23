package ir.baho.framework.dto;

import java.io.Serializable;

public record IdBiValue<ID extends Serializable & Comparable<ID>, S extends Serializable & Comparable<S>, H extends Serializable & Comparable<H>>
        (ID id, S value, H another) {

}
