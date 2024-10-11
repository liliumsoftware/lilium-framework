package ir.baho.framework.audit.impl;

import ir.baho.framework.audit.EnversRepository;
import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.domain.BaseEntitySimple;
import ir.baho.framework.domain.BaseRevisionEntity;
import ir.baho.framework.domain.RevisionEntityMetadata;
import ir.baho.framework.metadata.Constraint;
import ir.baho.framework.metadata.EnversMetadata;
import ir.baho.framework.metadata.EnversPageMetadata;
import ir.baho.framework.metadata.Metadata;
import ir.baho.framework.metadata.Search;
import ir.baho.framework.metadata.Sort;
import ir.baho.framework.repository.specification.PredicateAuditSpecification;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.criteria.AuditProperty;
import org.hibernate.envers.query.criteria.MatchMode;
import org.hibernate.envers.query.order.AuditOrder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.envers.repository.support.DefaultRevisionMetadata;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryImpl;
import org.springframework.data.history.AnnotationRevisionMetadata;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.history.Revisions;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.history.support.RevisionEntityInformation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.history.RevisionMetadata.RevisionType.DELETE;
import static org.springframework.data.history.RevisionMetadata.RevisionType.INSERT;
import static org.springframework.data.history.RevisionMetadata.RevisionType.UPDATE;

@Transactional(readOnly = true)
public class EnversRepositoryImpl<E extends BaseEntitySimple<?, ID>, ID extends Serializable & Comparable<ID>, N extends Number & Comparable<N>>
        extends EnversRevisionRepositoryImpl<E, ID, N> implements EnversRepository<E, ID, N> {

    private final EntityInformation<E, ?> entityInformation;
    private final EntityManager entityManager;
    private final List<StringConverter<? extends Comparable<?>>> converters;

    public EnversRepositoryImpl(JpaEntityInformation<E, ?> entityInformation,
                                RevisionEntityInformation revisionEntityInformation, EntityManager entityManager,
                                List<StringConverter<? extends Comparable<?>>> converters) {
        super(entityInformation, revisionEntityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.converters = converters;
    }

    @Override
    public Revisions<N, E> findRevisions(EnversMetadata metadata, PredicateAuditSpecification specification) {
        return findRevisions(null, metadata, specification);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Revisions<N, E> findRevisions(ID id, EnversMetadata metadata, PredicateAuditSpecification specification) {
        AuditQuery query = getQuery(id, metadata, specification);
        getSort(metadata).forEach(query::addOrder);

        List<Object[]> result = query.getResultList();
        List<Revision<N, E>> revisions = result.stream()
                .map(objects -> createRevision(new QueryResult<>(objects))).collect(Collectors.toList());
        return Revisions.of(revisions);
    }

    @Override
    public Page<Revision<N, E>> findRevisions(EnversPageMetadata metadata, PredicateAuditSpecification specification) {
        return findRevisions(null, metadata, specification);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Page<Revision<N, E>> findRevisions(ID id, EnversPageMetadata metadata, PredicateAuditSpecification specification) {
        AuditQuery query = getQuery(id, metadata, specification);
        query.setFirstResult(metadata.getPage());
        query.setMaxResults(metadata.getSize());
        getSort(metadata).forEach(query::addOrder);

        List<Object[]> result = query.getResultList();
        Long count = (Long) getQuery(id, metadata, specification).addProjection(AuditEntity.revisionNumber().count()).getSingleResult();

        List<Revision<N, E>> revisions = result.stream()
                .map(objects -> createRevision(new QueryResult<>(objects))).collect(Collectors.toList());
        return new PageImpl<>(revisions, getPageable(metadata), count);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected AuditQuery getQuery(ID id, EnversMetadata metadata, PredicateAuditSpecification specification) {
        Map<String, Class<?>> paths = new LinkedHashMap<>();
        Map<String, Constraint[]> constraints = new LinkedHashMap<>();
        AuditCriterion criterion = specification.predicate((path, type, constraint) -> {
            paths.put(path, type);
            if (constraint == null || constraint.length == 0) {
                constraint = Constraint.all();
            }
            constraints.put(path, constraint);
        });
        AuditReader reader = AuditReaderFactory.get(entityManager);
        AuditQuery query = reader.createQuery().forRevisionsOfEntity(entityInformation.getJavaType(), false, true);
        if (criterion != null) {
            query.add(criterion);
        }
        if (metadata.getSearch() != null) {
            if (metadata.notConverted()) {
                convert(metadata, paths, constraints, entityInformation.getJavaType(), converters);
            }
            AuditCriterion c = null;
            for (Map.Entry<String, Class<?>> path : paths.entrySet()) {
                for (Search s : metadata.getSearch()) {
                    if (path.getKey().equals(s.getField())) {
                        AuditProperty property = AuditEntity.property(s.getField());
                        switch (s.getConstraint()) {
                            case IS_NULL ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.isNull()) : AuditEntity.or(c, property.isNull()) : property.isNull();
                            case IS_NOT_NULL ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.isNotNull()) : AuditEntity.or(c, property.isNotNull()) : property.isNotNull();
                            case EQUALS ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.eq(s.getValue())) : AuditEntity.or(c, property.eq(s.getValue())) : property.eq(s.getValue());
                            case NOT_EQUALS ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.ne(s.getValue())) : AuditEntity.or(c, property.ne(s.getValue())) : property.ne(s.getValue());
                            case EQUALS_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.ilike(String.valueOf(s.getValue()), MatchMode.EXACT)) : AuditEntity.or(c, property.ilike(String.valueOf(s.getValue()), MatchMode.EXACT)) : property.ilike(String.valueOf(s.getValue()), MatchMode.EXACT);
                            case NOT_EQUALS_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.EXACT))) : AuditEntity.or(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.EXACT))) : AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.EXACT));
                            case GREATER_THAN ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.gt(s.getValue())) : AuditEntity.or(c, property.gt(s.getValue())) : property.gt(s.getValue());
                            case LESS_THAN ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.lt(s.getValue())) : AuditEntity.or(c, property.lt(s.getValue())) : property.lt(s.getValue());
                            case GREATER_THAN_OR_EQUALS ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.or(property.ge(s.getValue()), property.eq(s.getValue()))) : AuditEntity.or(c, AuditEntity.or(property.ge(s.getValue()), property.eq(s.getValue()))) : AuditEntity.or(property.ge(s.getValue()), property.eq(s.getValue()));
                            case LESS_THAN_OR_EQUALS ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.or(property.le(s.getValue()), property.eq(s.getValue()))) : AuditEntity.or(c, AuditEntity.or(property.le(s.getValue()), property.eq(s.getValue()))) : AuditEntity.or(property.le(s.getValue()), property.eq(s.getValue()));
                            case CONTAINS ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.like(String.valueOf(s.getValue()), MatchMode.ANYWHERE)) : AuditEntity.or(c, property.like(String.valueOf(s.getValue()), MatchMode.ANYWHERE)) : property.like(String.valueOf(s.getValue()), MatchMode.ANYWHERE);
                            case NOT_CONTAINS ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.ANYWHERE))) : AuditEntity.or(c, AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.ANYWHERE))) : AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.ANYWHERE));
                            case STARTS_WITH ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.like(String.valueOf(s.getValue()), MatchMode.START)) : AuditEntity.or(c, property.like(String.valueOf(s.getValue()), MatchMode.START)) : property.like(String.valueOf(s.getValue()), MatchMode.START);
                            case NOT_STARTS_WITH ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.START))) : AuditEntity.or(c, AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.START))) : AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.START));
                            case ENDS_WITH ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.like(String.valueOf(s.getValue()), MatchMode.END)) : AuditEntity.or(c, property.like(String.valueOf(s.getValue()), MatchMode.END)) : property.like(String.valueOf(s.getValue()), MatchMode.END);
                            case NOT_ENDS_WITH ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.END))) : AuditEntity.or(c, AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.END))) : AuditEntity.not(property.like(String.valueOf(s.getValue()), MatchMode.END));
                            case CONTAINS_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.ilike(String.valueOf(s.getValue()), MatchMode.ANYWHERE)) : AuditEntity.or(c, property.ilike(String.valueOf(s.getValue()), MatchMode.ANYWHERE)) : property.ilike(String.valueOf(s.getValue()), MatchMode.ANYWHERE);
                            case NOT_CONTAINS_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.ANYWHERE))) : AuditEntity.or(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.ANYWHERE))) : AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.ANYWHERE));
                            case STARTS_WITH_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.ilike(String.valueOf(s.getValue()), MatchMode.START)) : AuditEntity.or(c, property.ilike(String.valueOf(s.getValue()), MatchMode.START)) : property.ilike(String.valueOf(s.getValue()), MatchMode.START);
                            case NOT_STARTS_WITH_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.START))) : AuditEntity.or(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.START))) : AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.START));
                            case ENDS_WITH_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.ilike(String.valueOf(s.getValue()), MatchMode.END)) : AuditEntity.or(c, property.ilike(String.valueOf(s.getValue()), MatchMode.END)) : property.ilike(String.valueOf(s.getValue()), MatchMode.END);
                            case NOT_ENDS_WITH_IGNORE_CASE ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.END))) : AuditEntity.or(c, AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.END))) : AuditEntity.not(property.ilike(String.valueOf(s.getValue()), MatchMode.END));
                            case BETWEEN ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.between(s.getValue(), s.getAnother())) : AuditEntity.or(c, property.between(s.getValue(), s.getAnother())) : property.between(s.getValue(), s.getAnother());
                            case NOT_BETWEEN ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.between(s.getValue(), s.getAnother()))) : AuditEntity.or(c, AuditEntity.not(property.between(s.getValue(), s.getAnother()))) : AuditEntity.not(property.between(s.getValue(), s.getAnother()));
                            case IN ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, property.in(s.getValues())) : AuditEntity.or(c, property.in(s.getValues())) : property.in(s.getValues());
                            case NOT_IN ->
                                    c = c != null ? metadata.isAnd() ? AuditEntity.and(c, AuditEntity.not(property.in(s.getValues()))) : AuditEntity.or(c, AuditEntity.not(property.in(s.getValues()))) : AuditEntity.not(property.in(s.getValues()));
                        }
                        break;
                    }
                }
            }
            if (c != null) {
                query.add(c);
            }
        }
        if (id != null) {
            query.add(AuditEntity.property("id").eq(id));
        }
        if (metadata.getUsername() != null) {
            query.add(AuditEntity.revisionProperty("username").eq(metadata.getUsername()));
        }
        if (metadata.getFrom() != null) {
            query.add(AuditEntity.revisionProperty("timestamp").ge(getDate(metadata.getFrom())));
        }
        if (metadata.getTo() != null) {
            query.add(AuditEntity.revisionProperty("timestamp").le(getDate(metadata.getTo())));
        }
        if (metadata.getChangedProperty() != null) {
            for (String property : metadata.getChangedProperty()) {
                query.add(AuditEntity.property(property).hasChanged());
            }
        }
        query.add(AuditEntity.revisionType().in(Stream.of(metadata.getRevTypes())
                .map(QueryResult::convertRevisionType).toList()));
        return query;
    }

    protected Pageable getPageable(EnversPageMetadata metadata) {
        return PageRequest.of(metadata.getPage(), metadata.getSize());
    }

    @SuppressWarnings("unchecked")
    protected Revision<N, E> createRevision(QueryResult<E> queryResult) {
        return Revision.of((RevisionMetadata<N>) queryResult.createRevisionMetadata(), queryResult.entity);
    }

    protected List<AuditOrder> getSort(Metadata metadata) {
        List<AuditOrder> orders = new ArrayList<>();
        if (metadata.getSort() != null) {
            for (Sort sort : metadata.getSort()) {
                if (sort.getField().equals("timestamp")) {
                    orders.add(sort.isAsc() ? AuditEntity.revisionProperty("timestamp").asc() : AuditEntity.revisionProperty("timestamp").desc());
                } else {
                    orders.add(sort.isAsc() ? AuditEntity.property(sort.getField()).asc() : AuditEntity.property(sort.getField()).desc());
                }
            }
        }
        return orders;
    }

    private Date getDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(LocaleContextHolder.getTimeZone().toZoneId()).toInstant());
    }

    @SuppressWarnings("unchecked")
    protected static class QueryResult<E> {

        private final E entity;
        private final Object metadata;
        private final RevisionMetadata.RevisionType revisionType;

        QueryResult(Object[] data) {
            Assert.notNull(data, "Data must not be null");
            Assert.isTrue(data.length == 3, () -> String.format("Data must have length three, but has length %d.", data.length));
            Assert.isTrue(data[2] instanceof RevisionType, () -> String.format("The third array element must be of type Revision type, but is of type %s", data[2].getClass()));

            entity = (E) data[0];
            metadata = data[1];
            revisionType = convertRevisionType((RevisionType) data[2]);
        }

        private static RevisionMetadata.RevisionType convertRevisionType(RevisionType datum) {
            return switch (datum) {
                case ADD -> INSERT;
                case MOD -> UPDATE;
                case DEL -> DELETE;
            };
        }

        private static RevisionType convertRevisionType(RevisionMetadata.RevisionType datum) {
            return switch (datum) {
                case INSERT -> RevisionType.ADD;
                case UPDATE -> RevisionType.MOD;
                case DELETE -> RevisionType.DEL;
                default -> null;
            };
        }

        @SuppressWarnings("rawtypes")
        RevisionMetadata<?> createRevisionMetadata() {
            if (metadata instanceof DefaultRevisionEntity revisionEntity) {
                return new DefaultRevisionMetadata(revisionEntity, revisionType);
            } else if (metadata instanceof BaseRevisionEntity<?, ?> revisionEntity) {
                return new RevisionEntityMetadata(revisionEntity.getId(), revisionEntity.getUsername(),
                        LocalDateTime.ofInstant(revisionEntity.getTimestamp().toInstant(), LocaleContextHolder.getTimeZone().toZoneId()),
                        revisionType, revisionEntity);
            } else {
                return new AnnotationRevisionMetadata<>(metadata, RevisionNumber.class, RevisionTimestamp.class, revisionType);
            }
        }

    }

}
