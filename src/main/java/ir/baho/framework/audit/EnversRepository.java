package ir.baho.framework.audit;

import ir.baho.framework.domain.BaseEntitySimple;
import ir.baho.framework.metadata.EnversMetadata;
import ir.baho.framework.metadata.EnversPageMetadata;
import ir.baho.framework.repository.BaseRepository;
import ir.baho.framework.repository.specification.PredicateAuditSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.history.RevisionRepository;

import java.io.Serializable;

@NoRepositoryBean
public interface EnversRepository<E extends BaseEntitySimple<?, ID>, ID extends Serializable & Comparable<ID>, N extends Number & Comparable<N>>
        extends BaseRepository<E, ID>, RevisionRepository<E, ID, N> {

    Revisions<N, E> findRevisions(EnversMetadata metadata, PredicateAuditSpecification specification);

    Revisions<N, E> findRevisions(ID id, EnversMetadata metadata, PredicateAuditSpecification specification);

    Page<Revision<N, E>> findRevisions(EnversPageMetadata metadata, PredicateAuditSpecification specification);

    Page<Revision<N, E>> findRevisions(ID id, EnversPageMetadata metadata, PredicateAuditSpecification specification);

}
