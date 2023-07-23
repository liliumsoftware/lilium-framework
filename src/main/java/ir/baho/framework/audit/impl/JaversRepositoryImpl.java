package ir.baho.framework.audit.impl;

import ir.baho.framework.audit.JaversRepository;
import ir.baho.framework.domain.Entity;
import ir.baho.framework.domain.RevisionEntityMetadata;
import ir.baho.framework.metadata.JaversMetadata;
import ir.baho.framework.service.CurrentUser;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.metamodel.object.SnapshotType;
import org.javers.repository.jql.InstanceIdDTO;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.history.Revisions;
import org.springframework.data.repository.core.EntityInformation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JaversRepositoryImpl<E extends Entity<?, ID>, ID extends Serializable & Comparable<ID>> implements JaversRepository<E, ID> {

    private final EntityInformation<E, ?> entityInformation;
    private final CurrentUser currentUser;
    private final AuthorProvider authorProvider;
    private final Javers javers;
    private final CommitPropertiesProvider commitPropertiesProvider;

    public JaversRepositoryImpl(ApplicationContext applicationContext, EntityInformation<E, ?> entityInformation, CurrentUser currentUser) {
        this.entityInformation = entityInformation;
        this.currentUser = currentUser;
        this.authorProvider = applicationContext.getBean(AuthorProvider.class);
        this.javers = applicationContext.getBean(Javers.class);
        this.commitPropertiesProvider = applicationContext.getBean(CommitPropertiesProvider.class);
    }

    @Override
    public void commitSave(E entity) {
        javers.commit(authorProvider.provide(), entity, commitPropertiesProvider.provideForCommittedObject(entity));
    }

    @Override
    public void commitDelete(ID id) {
        javers.commitShallowDeleteById(authorProvider.provide(), InstanceIdDTO.instanceId(id, entityInformation.getJavaType()),
                commitPropertiesProvider.provideForDeleteById(entityInformation.getJavaType(), id));
    }

    @Override
    public Revisions<BigDecimal, E> findRevisions(JaversMetadata metadata) {
        List<Shadow<E>> shadows = javers.findShadows(getQuery(QueryBuilder.byClass(entityInformation.getJavaType()), metadata).build());
        return Revisions.of(shadows.stream()
                .map(sh -> Revision.of(getEntityMetadata(sh.getCommitMetadata()), sh.get()))
                .collect(Collectors.toList()));
    }

    @Override
    public Revisions<BigDecimal, E> findRevisions(ID id, JaversMetadata metadata) {
        List<Shadow<E>> shadows = javers.findShadows(getQuery(QueryBuilder.byInstanceId(id, entityInformation.getJavaType()), metadata).build());
        return Revisions.of(shadows.stream()
                .map(sh -> Revision.of(getEntityMetadata(sh.getCommitMetadata()), sh.get()))
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<Revision<BigDecimal, E>> findLastChangeRevision(ID id) {
        List<Shadow<E>> shadows = javers.findShadows(QueryBuilder.byInstanceId(id, entityInformation.getJavaType()).limit(1).build());
        if (shadows.isEmpty()) {
            return Optional.empty();
        }
        Shadow<E> shadow = shadows.get(0);
        return Optional.of(Revision.of(getEntityMetadata(shadow.getCommitMetadata()), shadow.get()));
    }

    @Override
    public Revisions<BigDecimal, E> findRevisions(ID id) {
        Stream<Shadow<E>> shadows = javers.findShadowsAndStream(QueryBuilder.byInstanceId(id, entityInformation.getJavaType()).build());
        return Revisions.of(shadows
                .map(sh -> Revision.of(getEntityMetadata(sh.getCommitMetadata()), sh.get()))
                .collect(Collectors.toList()));
    }

    @Override
    public Page<Revision<BigDecimal, E>> findRevisions(ID id, Pageable pageable) {
        Stream<Shadow<E>> shadows = javers.findShadowsAndStream(QueryBuilder.byInstanceId(id, entityInformation.getJavaType())
                .skip(pageable.getPageNumber() * pageable.getPageSize()).limit(pageable.getPageSize()).build());
        return new PageImpl<>(shadows
                .map(sh -> Revision.of(getEntityMetadata(sh.getCommitMetadata()), sh.get()))
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<Revision<BigDecimal, E>> findRevision(ID id, BigDecimal revisionNumber) {
        List<Shadow<E>> shadows = javers.findShadows(QueryBuilder.byInstanceId(id, entityInformation.getJavaType())
                .withCommitId(revisionNumber).limit(1).build());
        if (shadows.isEmpty()) {
            return Optional.empty();
        }
        Shadow<E> shadow = shadows.get(0);
        return Optional.of(Revision.of(getEntityMetadata(shadow.getCommitMetadata()), shadow.get()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CurrentUser> T currentUser() {
        return (T) currentUser;
    }

    private QueryBuilder getQuery(QueryBuilder queryBuilder, JaversMetadata metadata) {
        if (metadata.getUsername() != null) {
            queryBuilder = queryBuilder.byAuthor(metadata.getUsername());
        }
        if (metadata.getFrom() != null) {
            queryBuilder = queryBuilder.from(metadata.getFrom());
        }
        if (metadata.getTo() != null) {
            queryBuilder = queryBuilder.to(metadata.getTo());
        }
        if (metadata.getChangedProperty() != null) {
            queryBuilder = queryBuilder.withChangedPropertyIn(metadata.getChangedProperty());
        }
        if (metadata.getRevType() != null) {
            queryBuilder = queryBuilder.withSnapshotType(switch (metadata.getRevType()) {
                case INSERT -> SnapshotType.INITIAL;
                case UPDATE -> SnapshotType.UPDATE;
                case DELETE -> SnapshotType.TERMINAL;
                default -> null;
            });
        }
        return queryBuilder;
    }

    private RevisionEntityMetadata<?, BigDecimal> getEntityMetadata(CommitMetadata commitMetadata) {
        Map<String, String> properties = commitMetadata.getProperties();
        RevisionMetadata.RevisionType type = RevisionMetadata.RevisionType.UNKNOWN;
        if (properties != null && properties.get(RevisionMetadata.RevisionType.class.getSimpleName()) != null) {
            type = RevisionMetadata.RevisionType.valueOf(properties.get(RevisionMetadata.RevisionType.class.getSimpleName()));
        }
        return new RevisionEntityMetadata<>(commitMetadata.getId().valueAsNumber(), commitMetadata.getAuthor(),
                commitMetadata.getCommitDate(), type, commitMetadata);
    }

}
