package ir.baho.framework.mapper;

import ir.baho.framework.domain.IdProjection;
import ir.baho.framework.dto.BaseIdDto;
import ir.baho.framework.dto.IdBiValue;
import ir.baho.framework.dto.IdValue;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ProjectionMapper<E, D extends BaseIdDto<D, ?>> {

    D toDto(E dto);

    List<D> toDto(List<E> dto);

    default <T extends IdProjection<ID>, ID extends Serializable & Comparable<ID>, S extends Serializable & Comparable<S>>
    List<IdValue<ID, S>> toIdValues(List<T> values, Function<T, S> value) {
        return values.stream().map(v -> new IdValue<>(v.getId(), value.apply(v))).collect(Collectors.toList());
    }

    default <T extends IdProjection<ID>, ID extends Serializable & Comparable<ID>, S extends Serializable & Comparable<S>, H extends Serializable & Comparable<H>>
    List<IdBiValue<ID, S, H>> toIdBiValues(List<T> values, Function<T, S> value, Function<T, H> another) {
        return values.stream().map(v -> new IdBiValue<>(v.getId(), value.apply(v), another.apply(v))).collect(Collectors.toList());
    }

    default Duration toDuration(long millis) {
        return Duration.ofMillis(millis);
    }

    default long toMillis(Duration duration) {
        return duration == null ? 0 : duration.toMillis();
    }

    default DayOfWeek toDayOfWeek(int dayOfWeekValue) {
        return DayOfWeek.of(dayOfWeekValue);
    }

    default int toDayOfWeekValue(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue();
    }

}
