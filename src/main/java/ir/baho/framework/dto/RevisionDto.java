package ir.baho.framework.dto;

import java.io.Serializable;

public record RevisionDto<N extends Number & Comparable<N>, E>(E entity, RevisionMetadataDto<N> metadata)
        implements Serializable, Comparable<RevisionDto<N, E>> {

    @Override
    public int compareTo(RevisionDto<N, E> o) {
        return metadata().compareTo(o.metadata());
    }

}
