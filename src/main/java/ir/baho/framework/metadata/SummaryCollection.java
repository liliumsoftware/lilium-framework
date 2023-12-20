package ir.baho.framework.metadata;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class SummaryCollection<E, S> extends ArrayList<E> {

    private final S summary;

    public SummaryCollection(Collection<E> collection, S summary) {
        super(collection);
        this.summary = summary;
    }

    public <U, V> SummaryCollection<U, V> map(Function<? super E, ? extends U> converter, Function<? super S, ? extends V> summaryConverter) {
        return new SummaryCollection<>(stream().map(converter).collect(Collectors.toList()), summaryConverter.apply(summary));
    }

}
