package ir.baho.framework.mapper;

import ir.baho.framework.domain.Entity;
import ir.baho.framework.domain.RevisionEntityMetadata;
import ir.baho.framework.dto.BaseDtoSimple;
import ir.baho.framework.dto.RevisionDto;
import ir.baho.framework.dto.RevisionMetadataDto;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.history.Revisions;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface RevisionMapper {

    static <E extends Entity<E, ?>, D extends BaseDtoSimple<D, ?>, N extends Number & Comparable<N>>
    RevisionDto<N, D> toRevisionDto(Revision<N, E> revision, Function<E, D> toDto) {
        RevisionEntityMetadata<E, N> metadata = (RevisionEntityMetadata<E, N>) revision.getMetadata();
        return new RevisionDto<>(toDto.apply(revision.getEntity()), new RevisionMetadataDto<>(metadata.id(),
                metadata.username(), metadata.dateTime(), toRevisionType(metadata.revisionType())));
    }

    static <E extends Entity<E, ?>, D extends BaseDtoSimple<D, ?>, N extends Number & Comparable<N>>
    List<RevisionDto<N, D>> toRevisionDto(Revisions<N, E> revisions, Function<E, D> toDto) {
        return revisions.stream().map(revision -> toRevisionDto(revision, toDto)).collect(Collectors.toList());
    }

    private static RevisionMetadataDto.RevisionType toRevisionType(RevisionMetadata.RevisionType type) {
        return switch (type) {
            case INSERT -> RevisionMetadataDto.RevisionType.INSERT;
            case UPDATE -> RevisionMetadataDto.RevisionType.UPDATE;
            case DELETE -> RevisionMetadataDto.RevisionType.DELETE;
            default -> RevisionMetadataDto.RevisionType.UNKNOWN;
        };
    }

}
