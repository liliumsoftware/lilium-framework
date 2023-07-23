package ir.baho.framework.repository;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.exception.MetadataConstraintAccessException;
import ir.baho.framework.exception.MetadataConvertException;
import ir.baho.framework.exception.MetadataFieldAccessException;
import ir.baho.framework.metadata.Constraint;
import ir.baho.framework.metadata.Metadata;
import ir.baho.framework.metadata.PageSortMetadata;
import ir.baho.framework.metadata.Search;
import ir.baho.framework.metadata.SortMetadata;
import ir.baho.framework.service.CurrentUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BaseRepository<E, ID> extends Repository<E, ID> {

    <T extends CurrentUser> T currentUser();

    default void convert(Metadata metadata, Map<String, Class<?>> paths, Map<String, Constraint[]> constraints,
                         Class<?> domain, List<StringConverter<? extends Comparable<?>>> converters) {
        for (Search search : metadata.getSearch()) {
            Class<?> field = paths.get(search.getField());
            if (field == null) {
                throw new MetadataFieldAccessException(domain, search.getField());
            }

            if (Stream.of(constraints.get(search.getField())).noneMatch(c -> c == search.getConstraint())) {
                throw new MetadataConstraintAccessException(domain, search.getField(), search.getConstraint());
            }

            if (!CharSequence.class.isAssignableFrom(field) && Stream.of(Constraint.stringOnly()).anyMatch(c -> c == search.getConstraint())) {
                throw new MetadataConstraintAccessException(domain, search.getField(), search.getConstraint());
            }

            try {
                StringConverter<? extends Comparable<?>> converter = converters.stream()
                        .filter(c -> c.isSupported(field))
                        .findFirst().orElseThrow(() -> new MetadataConvertException(domain, search.getField(), search.getValue()));
                if (search.getConstraint() != Constraint.IS_NULL && search.getConstraint() != Constraint.IS_NOT_NULL) {
                    if (search.getConstraint() == Constraint.BETWEEN || search.getConstraint() == Constraint.NOT_BETWEEN) {
                        search.setValue(converter.convert(String.valueOf(search.getValue())));
                        search.setAnother(converter.convert(String.valueOf(search.getAnother())));
                    } else if (search.getConstraint() == Constraint.IN || search.getConstraint() == Constraint.NOT_IN) {
                        search.setValues(search.getValues().stream().map(String::valueOf).map(converter::convert).collect(Collectors.toList()));
                    } else {
                        search.setValue(converter.convert(String.valueOf(search.getValue())));
                    }
                }
            } catch (Exception e) {
                throw new MetadataConvertException(domain, search.getField(), search.getValue());
            }
        }
        metadata.converted();
    }

    default Pageable getPageable(PageSortMetadata metadata) {
        return PageRequest.of(metadata.getPage(), metadata.getSize(), getSort(metadata));
    }

    default Sort getSort(SortMetadata metadata) {
        return metadata.getSort() == null || metadata.getSort().length == 0 ? Sort.unsorted() :
                Sort.by(Stream.of(metadata.getSort()).filter(s -> s.getField() != null)
                        .map(e -> e.isAsc() ? Sort.Order.asc(e.getField()) : Sort.Order.desc(e.getField())).toArray(Sort.Order[]::new));
    }

    default Sort getSort(SortMetadata metadata, Map<String, Class<?>> paths, Class<?> domain) {
        if (metadata.getSort() == null || metadata.getSort().length == 0) {
            return Sort.unsorted();
        }
        Arrays.stream(metadata.getSort()).filter(sort -> paths.get(sort.getField()) == null).findAny().ifPresent(sort -> {
            throw new MetadataFieldAccessException(domain, sort.getField());
        });
        return Sort.by(Stream.of(metadata.getSort()).filter(s -> s.getField() != null)
                .map(e -> e.isAsc() ? Sort.Order.asc(e.getField()) : Sort.Order.desc(e.getField())).toArray(Sort.Order[]::new));
    }

    default boolean isNumber(Class<?> type) {
        return Number.class.isAssignableFrom(type)
                || byte.class == type || short.class == type
                || int.class == type || long.class == type
                || float.class == type || double.class == type;
    }

}
