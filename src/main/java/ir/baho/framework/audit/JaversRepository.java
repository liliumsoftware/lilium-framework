package ir.baho.framework.audit;

import ir.baho.framework.domain.Entity;
import ir.baho.framework.metadata.JaversMetadata;
import ir.baho.framework.repository.BaseRepository;
import org.springframework.data.history.Revisions;
import org.springframework.data.repository.history.RevisionRepository;

import java.io.Serializable;
import java.math.BigDecimal;

public interface JaversRepository<E extends Entity<?, ID>, ID extends Serializable & Comparable<ID>>
        extends BaseRepository<E, ID>, RevisionRepository<E, ID, BigDecimal> {

    void commitSave(E entity);

    void commitDelete(ID id);

    Revisions<BigDecimal, E> findRevisions(JaversMetadata metadata);

    Revisions<BigDecimal, E> findRevisions(ID id, JaversMetadata metadata);

}
