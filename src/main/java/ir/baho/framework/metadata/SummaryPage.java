package ir.baho.framework.metadata;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.function.Function;

@Getter
public class SummaryPage<E, S> extends PageImpl<E> {

    private final S summary;

    public SummaryPage(Page<E> page, S summary) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
        this.summary = summary;
    }

    public <U, V> SummaryPage<U, V> map(Function<? super E, ? extends U> converter, Function<? super S, ? extends V> summaryConverter) {
        return new SummaryPage<>(super.map(converter), summaryConverter.apply(summary));
    }

}
