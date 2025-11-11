package ir.baho.framework.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record EntityMetadata<ID extends Serializable & Comparable<ID>>(
        ID id, LocalDateTime lastModifiedDate, int version) implements Serializable {
}
