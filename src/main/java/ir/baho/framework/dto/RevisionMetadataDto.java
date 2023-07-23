package ir.baho.framework.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record RevisionMetadataDto<N extends Number & Comparable<N>>
        (N id, String username, LocalDateTime dateTime, RevisionType type)
        implements Serializable, Comparable<RevisionMetadataDto<N>> {

    @Override
    public int compareTo(RevisionMetadataDto<N> o) {
        return id().compareTo(o.id());
    }

    public enum RevisionType {
        UNKNOWN,
        INSERT,
        UPDATE,
        DELETE
    }

}
