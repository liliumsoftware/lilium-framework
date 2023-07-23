package ir.baho.framework.domain;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public record RevisionEntityMetadata<R, N extends Number & Comparable<N>>
        (N id, String username, LocalDateTime dateTime, RevisionType revisionType, R delegate)
        implements RevisionMetadata<N> {

    @Override
    public Optional<N> getRevisionNumber() {
        return Optional.of(id);
    }

    @Override
    public Optional<Instant> getRevisionInstant() {
        return Optional.of(dateTime.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toInstant());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDelegate() {
        return (T) delegate;
    }

}
