package ir.baho.framework.repository.impl;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.domain.BaseProjection;
import ir.baho.framework.domain.Entity;
import ir.baho.framework.exception.MetadataFieldAccessException;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.metadata.Constraint;
import ir.baho.framework.metadata.ExportType;
import ir.baho.framework.metadata.Metadata;
import ir.baho.framework.metadata.PageMetadata;
import ir.baho.framework.metadata.ProjectionMetadata;
import ir.baho.framework.metadata.ProjectionPageMetadata;
import ir.baho.framework.metadata.ReportMetadata;
import ir.baho.framework.metadata.StaticReportMetadata;
import ir.baho.framework.metadata.SummaryPage;
import ir.baho.framework.metadata.report.ReportDesign;
import ir.baho.framework.repository.JpaCriteriaRepository;
import ir.baho.framework.repository.specification.ExpressionPath;
import ir.baho.framework.repository.specification.PredicateCriteriaSpecification;
import ir.baho.framework.repository.specification.Predicates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.SneakyThrows;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSaver;
import org.hibernate.metamodel.mapping.MappingModelExpressible;
import org.hibernate.query.sqm.internal.QuerySqmImpl;
import org.hibernate.query.sqm.internal.SqmUtil;
import org.hibernate.query.sqm.spi.SqmParameterMappingModelResolutionAccess;
import org.hibernate.query.sqm.sql.SqmTranslation;
import org.hibernate.query.sqm.sql.SqmTranslator;
import org.hibernate.query.sqm.sql.internal.StandardSqmTranslator;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.sql.ast.tree.select.SelectStatement;
import org.hibernate.sql.exec.spi.JdbcOperation;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional(readOnly = true)
public class JpaCriteriaRepositoryImpl<E extends Entity<?, ID>, ID extends Serializable & Comparable<ID>>
        extends SimpleJpaRepository<E, ID> implements JpaCriteriaRepository<E, ID> {

    private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    private final EntityManager entityManager;
    private final PersistenceProvider provider;
    private final MessageResource messageResource;
    private final List<StringConverter<? extends Comparable<?>>> converters;

    public JpaCriteriaRepositoryImpl(JpaEntityInformation<E, ?> entityInformation, EntityManager entityManager,
                                     MessageResource messageResource, List<StringConverter<? extends Comparable<?>>> converters) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.provider = PersistenceProvider.fromEntityManager(entityManager);
        this.messageResource = messageResource;
        this.converters = converters;
    }

    private static long executeCountQuery(TypedQuery<Long> query) {
        Assert.notNull(query, "TypedQuery must not be null");

        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }
        return total;
    }

    @Override
    public void detach(E entity) {
        entityManager.detach(entity);
    }

    @Override
    public void refresh(E entity) {
        entityManager.refresh(entity);
    }

    @Override
    public E findOne(Metadata metadata, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        TypedQuery<E> query = getQuery(spec, getSort(metadata));
        return query.getSingleResult();
    }

    @Override
    public Page<E> findAll(PageMetadata metadata, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        Pageable pageable = getPageable(metadata);
        TypedQuery<E> query = getQuery(spec, pageable);
        return pageable.isUnpaged() ? new PageImpl<>(query.getResultList()) : readPage(query, getDomainClass(), pageable, spec);
    }

    @Override
    public <S> SummaryPage<E, S> findAll(PageMetadata metadata, PredicateCriteriaSpecification<E> specification, Class<S> summaryClass) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Map<String, Expression<?>> summaryFields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, summaryFields);
        Pageable pageable = getPageable(metadata);
        TypedQuery<E> query = getQuery(spec, pageable);

        S summary;
        if (BaseProjection.class.isAssignableFrom(summaryClass)) {
            TypedQuery<S> summaryQuery = getQuery(summaryClass, spec, summaryFields);
            summary = summaryQuery.getSingleResult();
        } else {
            TypedQuery<Tuple> summaryQuery = getQuery(spec, summaryFields);
            summary = processSingleResult(summaryClass, summaryQuery);
        }
        return new SummaryPage<>(pageable.isUnpaged() ? new PageImpl<>(query.getResultList()) : readPage(query, getDomainClass(), pageable, spec), summary);
    }

    @Override
    public List<E> findAll(Metadata metadata, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        TypedQuery<E> query = getQuery(spec, getSort(metadata));
        return query.getResultList();
    }

    @Override
    public <P> P findOne(Metadata metadata, Class<P> projection, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        if (BaseProjection.class.isAssignableFrom(projection)) {
            TypedQuery<P> query = getQuery(projection, spec, fields);
            return query.getSingleResult();
        } else {
            TypedQuery<Tuple> query = getQuery(spec, fields);
            return processSingleResult(projection, query);
        }
    }

    @Override
    public <P> Page<P> findAll(PageMetadata metadata, Class<P> projection, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        Pageable pageable = getPageable(metadata);
        if (BaseProjection.class.isAssignableFrom(projection)) {
            TypedQuery<P> query = getQuery(projection, spec, fields);
            return pageable.isUnpaged() ? new PageImpl<>(query.getResultList()) : getPage(projection, query, pageable, spec);
        } else {
            TypedQuery<Tuple> query = getQuery(spec, fields);
            return pageable.isUnpaged() ? new PageImpl<>(processResultList(projection, query)) : getPage(projection, query, pageable, spec);
        }
    }

    @Override
    public <P> List<P> findAll(Metadata metadata, Class<P> projection, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        if (BaseProjection.class.isAssignableFrom(projection)) {
            TypedQuery<P> query = getQuery(projection, spec, fields);
            return query.getResultList();
        } else {
            TypedQuery<Tuple> query = getQuery(spec, fields);
            return processResultList(projection, query);
        }
    }

    @Override
    public Map<String, Object> findOne(ProjectionMetadata metadata, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        TypedQuery<Object[]> query = getQuery(metadata, spec, fields);
        return processSingleResult(metadata, query);
    }

    @Override
    public Page<Map<String, Object>> findAll(ProjectionPageMetadata metadata, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        Pageable pageable = getPageable(metadata);
        TypedQuery<Object[]> query = getQuery(metadata, spec, fields);
        return pageable.isUnpaged() ? new PageImpl<>(processResultList(metadata, query)) : getPage(metadata, query, pageable, spec);
    }

    @Override
    public List<Map<String, Object>> findAll(ProjectionMetadata metadata, PredicateCriteriaSpecification<E> specification) {
        Map<String, Expression<?>> fields = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, fields, new HashMap<>());
        TypedQuery<Object[]> query = getQuery(metadata, spec, fields);
        return processResultList(metadata, query);
    }

    @SneakyThrows
    @Override
    public JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, PredicateCriteriaSpecification<E> specification) {
        return getJasperReportBuilder(metadata, design, specification);
    }

    @SneakyThrows
    @Override
    public JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, InputStream inputStream, PredicateCriteriaSpecification<E> specification) {
        return getJasperReportBuilder(metadata, design, specification).setTemplateDesign(inputStream);
    }

    @SneakyThrows
    @Override
    public JasperPrint report(StaticReportMetadata metadata, InputStream inputStream) {
        JRFileVirtualizer virtualizer = new JRFileVirtualizer(VIRTUALIZER_MAX_SIZE, VIRTUALIZER_TEMP_DIR);
        try (Connection connection = getConnection()) {
            metadata.param(JRParameter.REPORT_VIRTUALIZER, virtualizer);
            metadata.param(JRParameter.REPORT_LOCALE, metadata.getLocale());
            return JasperFillManager.fillReport(inputStream, metadata.getParams(), connection);
        } finally {
            virtualizer.cleanup();
        }
    }

    @SneakyThrows
    @Override
    public void save(java.nio.file.Path path, JasperReportBuilder builder) {
        JRFileVirtualizer virtualizer = new JRFileVirtualizer(VIRTUALIZER_MAX_SIZE, VIRTUALIZER_TEMP_DIR);
        Map<String, Object> parameters = builder.getJasperParameters();
        parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
        try (Connection connection = getConnection()) {
            JRSaver.saveObject(JasperFillManager.fillReport(builder.toJasperReport(), parameters, connection), path.toFile());
        } finally {
            virtualizer.cleanup();
        }
    }

    @SneakyThrows
    @Override
    public void export(JasperReportBuilder builder, ExportType type, OutputStream outputStream) {
        JRFileVirtualizer virtualizer = new JRFileVirtualizer(VIRTUALIZER_MAX_SIZE, VIRTUALIZER_TEMP_DIR);
        Map<String, Object> parameters = builder.getJasperParameters();
        parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
        try (Connection connection = getConnection()) {
            export(JasperFillManager.fillReport(builder.toJasperReport(), parameters, connection), type, outputStream);
        } finally {
            virtualizer.cleanup();
        }
    }

    protected JasperReportBuilder getJasperReportBuilder(ReportMetadata metadata, ReportDesign design, PredicateCriteriaSpecification<E> specification) {
        metadata.report();
        Map<String, Expression<?>> expressions = new LinkedHashMap<>();
        Specification<E> spec = getSpecification(metadata, specification, expressions, new HashMap<>());
        TypedQuery<Object[]> query = getQuery(metadata, spec, expressions);

        return getDesign(metadata, design, messageResource, converters,
                expressions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getJavaType())),
                getDomainClass(), false)
                .setReportName(metadata.getName())
                .setLocale(metadata.getLocale())
                .setQuery(getQueryString(query));
    }

    @SneakyThrows
    protected Connection getConnection() {
        EntityManagerFactoryInfo entityManagerFactoryInfo = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
        return Objects.requireNonNull(entityManagerFactoryInfo.getDataSource()).getConnection();
    }

    protected <P> TypedQuery<P> getQuery(Class<P> projection, Specification<E> spec, Map<String, Expression<?>> fields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<P> query = builder.createQuery(projection);

        applySpecificationToCriteria(spec, getDomainClass(), query);

        Expression<?>[] selections = fields.values().toArray(Expression<?>[]::new);
        Stream.of(selections).forEach(selection -> selection.alias(JpaCriteriaRepository.getExpressionName(selection)));
        query.select(builder.construct(projection, selections));

        return applyRepositoryMethodMetadata(entityManager.createQuery(query));
    }

    protected TypedQuery<Tuple> getQuery(Specification<E> spec, Map<String, Expression<?>> fields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);

        applySpecificationToCriteria(spec, getDomainClass(), query);

        Expression<?>[] selections = fields.values().toArray(Expression<?>[]::new);
        Stream.of(selections).forEach(selection -> selection.alias(JpaCriteriaRepository.getExpressionName(selection)));
        query.select(builder.tuple(selections));

        return applyRepositoryMethodMetadata(entityManager.createQuery(query));
    }

    protected TypedQuery<Object[]> getQuery(ProjectionMetadata metadata, Specification<E> spec, Map<String, Expression<?>> fields) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);

        applySpecificationToCriteria(spec, getDomainClass(), query);

        fields.values().forEach(expression -> fields.put(JpaCriteriaRepository.getExpressionName(expression), expression));
        query.select(builder.array(getSelections(metadata.getField(), fields.values())));

        return applyRepositoryMethodMetadata(entityManager.createQuery(query));
    }

    @SuppressWarnings("unchecked")
    protected <P> Page<P> getPage(Class<P> projection, TypedQuery<?> query, Pageable pageable, @Nullable Specification<E> spec) {
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        if (BaseProjection.class.isAssignableFrom(projection)) {
            return PageableExecutionUtils.getPage((List<P>) query.getResultList(), pageable, () -> executeCountQuery(getCountQuery(spec, getDomainClass())));
        } else {
            List<P> result = processResultList(projection, query);
            return PageableExecutionUtils.getPage(result, pageable, () -> executeCountQuery(getCountQuery(spec, getDomainClass())));
        }
    }

    protected Page<Map<String, Object>> getPage(ProjectionMetadata metadata, TypedQuery<Object[]> query, Pageable pageable, @Nullable Specification<E> spec) {
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        List<Map<String, Object>> result = processResultList(metadata, query);
        return PageableExecutionUtils.getPage(result, pageable, () -> executeCountQuery(getCountQuery(spec, getDomainClass())));
    }

    protected <P> P processSingleResult(Class<P> projection, TypedQuery<?> query) {
        ReturnedType returnedType = ReturnedType.of(projection, getDomainClass(), projectionFactory);
        ResultProcessor resultProcessor = new ResultProcessor(projectionFactory, returnedType);
        return resultProcessor.processResult(query.getSingleResult(), new TupleConverter(returnedType));
    }

    protected <P> List<P> processResultList(Class<P> projection, TypedQuery<?> query) {
        ReturnedType returnedType = ReturnedType.of(projection, getDomainClass(), projectionFactory);
        ResultProcessor resultProcessor = new ResultProcessor(projectionFactory, returnedType);
        List<P> result = resultProcessor.processResult(query.getResultList(), new TupleConverter(returnedType));
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    protected Map<String, Object> processSingleResult(ProjectionMetadata metadata, TypedQuery<Object[]> query) {
        Map<String, Object> result = new LinkedHashMap<>();
        Object[] objects = query.getSingleResult();
        for (int i = 0; i < objects.length; i++) {
            result.put(metadata.getField()[i], objects[i]);
        }
        return result;
    }

    protected List<Map<String, Object>> processResultList(ProjectionMetadata metadata, TypedQuery<Object[]> query) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] objects : query.getResultList()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < metadata.getField().length; i++) {
                map.put(metadata.getField()[i], objects[i]);
            }
            result.add(map);
        }
        return result;
    }

    protected Expression<?>[] getSelections(String[] fields, Collection<? extends Expression<?>> selections) {
        List<Expression<?>> expressions = new ArrayList<>();
        for (String field : fields) {
            Expression<?> selection = null;
            for (Expression<?> expression : selections) {
                if (JpaCriteriaRepository.getExpressionName(expression).equals(field)) {
                    selection = expression;
                }
            }
            if (selection == null) {
                throw new MetadataFieldAccessException(getDomainClass(), field);
            }
            expressions.add(selection);
        }
        return expressions.toArray(Expression<?>[]::new);
    }

    protected <S, U> void applySpecificationToCriteria(@Nullable Specification<U> spec, Class<U> domainClass, CriteriaQuery<S> query) {
        Assert.notNull(domainClass, "Domain class must not be null");
        Assert.notNull(query, "CriteriaQuery must not be null");

        Root<U> root = query.from(domainClass);

        if (spec == null) {
            return;
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);

        if (predicate != null) {
            query.where(predicate);
        }
    }

    protected <S> TypedQuery<S> applyRepositoryMethodMetadata(TypedQuery<S> query) {
        CrudMethodMetadata metadata = getRepositoryMethodMetadata();
        if (metadata == null) {
            return query;
        }

        LockModeType type = metadata.getLockModeType();
        TypedQuery<S> toReturn = type == null ? query : query.setLockMode(type);

        getQueryHints().withFetchGraphs(entityManager).forEach(query::setHint);
        if (metadata.getComment() != null && provider.getCommentHintKey() != null) {
            query.setHint(provider.getCommentHintKey(), provider.getCommentHintValue(metadata.getComment()));
        }

        return toReturn;
    }

    protected <P> String getQueryString(TypedQuery<P> query) {
        QuerySqmImpl<?> sqm = (QuerySqmImpl<?>) query;
        SqmTranslator<SelectStatement> translator = new StandardSqmTranslator<>(
                sqm.getSqmStatement(),
                sqm.getQueryOptions(),
                sqm.getDomainParameterXref(),
                sqm.getParameterBindings(),
                sqm.getLoadQueryInfluencers(),
                sqm.getSessionFactory(), true
        );
        SqmTranslation<SelectStatement> translation = translator.translate();
        JdbcParameterBindings jdbcParameterBindings = SqmUtil.createJdbcParameterBindings(
                sqm.getParameterBindings(),
                sqm.getDomainParameterXref(),
                SqmUtil.generateJdbcParamsXref(sqm.getDomainParameterXref(), translator),
                sqm.getSession().getFactory().getRuntimeMetamodels().getMappingMetamodel(),
                translation.getFromClauseAccess()::findTableGroup, new SqmParameterMappingModelResolutionAccess() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> MappingModelExpressible<T> getResolvedMappingModelType(SqmParameter<T> parameter) {
                        return (MappingModelExpressible<T>) translation.getSqmParameterMappingModelTypeResolutions().get(parameter);
                    }
                },
                sqm.getSession());

        JdbcOperation select = new QueryTranslator(sqm.getSessionFactory(), translation.getSqlAst())
                .translate(jdbcParameterBindings, sqm.getQueryOptions());
        return select.getSqlString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Specification<E> getSpecification(Metadata metadata, PredicateCriteriaSpecification<E> specification,
                                                Map<String, Expression<?>> expressions, Map<String, Expression<?>> summaryExpressions) {
        Map<String, Constraint[]> constraints = new HashMap<>();
        List<String> functions = new ArrayList<>();
        return (r, cq, cb) -> {
            Predicates predicates = specification.predicate(r, cq, cb, new ExpressionPath() {
                @Override
                public void add(Path<?> path, Constraint... constraint) {
                    String name = JpaCriteriaRepository.getExpressionName(path);
                    expressions.put(name, path);
                    if (constraint == null || constraint.length == 0) {
                        constraint = Constraint.all();
                    }
                    constraints.put(name, constraint);
                }

                @Override
                public void exp(Expression<?> expression, String name, Constraint... constraint) {
                    expression.alias(name);
                    expressions.put(name, expression);
                    if (constraint == null || constraint.length == 0) {
                        constraint = Constraint.all();
                    }
                    constraints.put(name, constraint);
                }

                @Override
                public void func(Expression<?> expression, String name, Constraint... constraint) {
                    functions.add(name);
                    expression.alias(name);
                    expressions.put(name, expression);
                    if (constraint == null || constraint.length == 0) {
                        constraint = Constraint.all();
                    }
                    constraints.put(name, constraint);
                }

                @Override
                public void summary(Expression<?> expression, String name) {
                    expression.alias(name);
                    summaryExpressions.put(name, expression);
                    expressions.put(name, expression);
                }

            });
            if (metadata.getSort() != null) {
                Stream.of(metadata.getSort()).forEach(sort -> {
                    Expression<?> expression = expressions.get(sort.getField());
                    if (expression == null) {
                        throw new MetadataFieldAccessException(getDomainClass(), sort.getField());
                    }
                    cq.orderBy(sort.isAsc() ? cb.asc(expression) : cb.desc(expression));
                });
            }
            List<Predicate> wherePredicates = new ArrayList<>();
            List<Predicate> havingPredicates = new ArrayList<>();
            if (predicates != null) {
                if (predicates.wherePredicate() != null) {
                    wherePredicates.add(predicates.wherePredicate());
                }
                if (predicates.havingPredicate() != null) {
                    havingPredicates.add(predicates.havingPredicate());
                }
            }
            if (metadata.getSearch() != null) {
                if (metadata.notConverted()) {
                    convert(metadata, expressions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getJavaType())), constraints, getDomainClass(), converters);
                }
                for (Map.Entry<String, Expression<?>> expressionEntry : expressions.entrySet()) {
                    Stream.of(metadata.getSearch()).filter(s -> expressionEntry.getKey().equals(s.getField())).forEach(search -> {
                        Expression<?> expression = expressionEntry.getValue();
                        boolean isFunction = functions.stream().anyMatch(f -> search.getField().equals(f));
                        switch (search.getConstraint()) {
                            case IS_NULL ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.isNull(expression));
                            case IS_NOT_NULL ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.isNotNull(expression));
                            case EQUALS -> {
                                if (expression.getJavaType() == Boolean.class || expression.getJavaType() == boolean.class) {
                                    if (isFunction) {
                                        havingPredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isTrue((Path<Boolean>) expression) : cb.isFalse((Path<Boolean>) expression));
                                    } else {
                                        wherePredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isTrue((Path<Boolean>) expression) : cb.isFalse((Path<Boolean>) expression));
                                    }
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.equal(expression, search.getValue()));
                                }
                            }
                            case NOT_EQUALS -> {
                                if (expression.getJavaType() == Boolean.class || expression.getJavaType() == boolean.class) {
                                    if (isFunction) {
                                        havingPredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isFalse((Path<Boolean>) expression) : cb.isTrue((Path<Boolean>) expression));
                                    } else {
                                        wherePredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isFalse((Path<Boolean>) expression) : cb.isTrue((Path<Boolean>) expression));
                                    }
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notEqual(expression, search.getValue()));
                                }
                            }
                            case EQUALS_IGNORE_CASE -> {
                                if (expression.getJavaType() == Boolean.class || expression.getJavaType() == boolean.class) {
                                    if (isFunction) {
                                        havingPredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isTrue((Path<Boolean>) expression) : cb.isFalse((Path<Boolean>) expression));
                                    } else {
                                        wherePredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isTrue((Path<Boolean>) expression) : cb.isFalse((Path<Boolean>) expression));
                                    }
                                } else if (CharSequence.class.isAssignableFrom(expression.getJavaType())) {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.equal(cb.upper((Path<String>) expression), search.getValue().toString().toUpperCase()));
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.equal(expression, search.getValue()));
                                }
                            }
                            case NOT_EQUALS_IGNORE_CASE -> {
                                if (expression.getJavaType() == Boolean.class || expression.getJavaType() == boolean.class) {
                                    if (isFunction) {
                                        havingPredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isFalse((Path<Boolean>) expression) : cb.isTrue((Path<Boolean>) expression));
                                    } else {
                                        wherePredicates.add("true".equalsIgnoreCase(String.valueOf(search.getValue())) ?
                                                cb.isFalse((Path<Boolean>) expression) : cb.isTrue((Path<Boolean>) expression));
                                    }
                                } else if (CharSequence.class.isAssignableFrom(expression.getJavaType())) {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notEqual(cb.upper((Path<String>) expression), search.getValue().toString().toUpperCase()));
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notEqual(expression, search.getValue()));
                                }
                            }
                            case GREATER_THAN -> {
                                if (isNumber(expression.getJavaType())) {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.gt((Expression<Number>) expression, (Number) search.getValue()));
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.greaterThan((Expression<Comparable>) expression, (Comparable) search.getValue()));
                                }
                            }
                            case LESS_THAN -> {
                                if (isNumber(expression.getJavaType())) {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.lt((Expression<Number>) expression, (Number) search.getValue()));
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.lessThan((Expression<Comparable>) expression, (Comparable) search.getValue()));
                                }
                            }
                            case GREATER_THAN_OR_EQUALS -> {
                                if (isNumber(expression.getJavaType())) {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.ge((Expression<Number>) expression, (Number) search.getValue()));
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.greaterThanOrEqualTo((Expression<Comparable>) expression, (Comparable) search.getValue()));
                                }
                            }
                            case LESS_THAN_OR_EQUALS -> {
                                if (isNumber(expression.getJavaType())) {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.le((Expression<Number>) expression, (Number) search.getValue()));
                                } else {
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.lessThanOrEqualTo((Expression<Comparable>) expression, (Comparable) search.getValue()));
                                }
                            }
                            case CONTAINS ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.like((Path<String>) expression, "%" + search.getValue() + "%"));
                            case NOT_CONTAINS ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notLike((Path<String>) expression, "%" + search.getValue() + "%"));
                            case STARTS_WITH ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.like((Path<String>) expression, search.getValue() + "%"));
                            case NOT_STARTS_WITH ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notLike((Path<String>) expression, search.getValue() + "%"));
                            case ENDS_WITH ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.like((Path<String>) expression, "%" + search.getValue()));
                            case NOT_ENDS_WITH ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notLike((Path<String>) expression, "%" + search.getValue()));
                            case CONTAINS_IGNORE_CASE ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.like(cb.upper((Path<String>) expression), ("%" + search.getValue() + "%").toUpperCase()));
                            case NOT_CONTAINS_IGNORE_CASE ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notLike(cb.upper((Path<String>) expression), ("%" + search.getValue() + "%").toUpperCase()));
                            case STARTS_WITH_IGNORE_CASE ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.like(cb.upper((Path<String>) expression), (search.getValue() + "%").toUpperCase()));
                            case NOT_STARTS_WITH_IGNORE_CASE ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notLike(cb.upper((Path<String>) expression), (search.getValue() + "%").toUpperCase()));
                            case ENDS_WITH_IGNORE_CASE ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.like(cb.upper((Path<String>) expression), ("%" + search.getValue()).toUpperCase()));
                            case NOT_ENDS_WITH_IGNORE_CASE ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.notLike(cb.upper((Path<String>) expression), ("%" + search.getValue()).toUpperCase()));
                            case BETWEEN ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.between((Expression<Comparable>) expression, (Comparable) search.getValue(), (Comparable) search.getAnother()));
                            case NOT_BETWEEN ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.not(cb.between((Expression<Comparable>) expression, (Comparable) search.getValue(), (Comparable) search.getAnother())));
                            case IN ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, expression.in(search.getValues()));
                            case NOT_IN ->
                                    addPredicate(isFunction, wherePredicates, havingPredicates, cb.not(expression.in(search.getValues())));
                        }
                    });
                }
            }
            if (metadata.isAnd()) {
                if (!havingPredicates.isEmpty()) {
                    cq.having(cb.and(havingPredicates.toArray(Predicate[]::new)));
                }
                return cb.and(wherePredicates.toArray(Predicate[]::new));
            } else {
                if (!havingPredicates.isEmpty()) {
                    cq.having(cb.or(havingPredicates.toArray(Predicate[]::new)));
                }
                return cb.or(wherePredicates.toArray(Predicate[]::new));
            }
        };
    }

    private void addPredicate(boolean isFunction, List<Predicate> wherePredicates, List<Predicate> havingPredicates, Predicate predicate) {
        if (isFunction) {
            havingPredicates.add(predicate);
        } else {
            wherePredicates.add(predicate);
        }
    }

}
