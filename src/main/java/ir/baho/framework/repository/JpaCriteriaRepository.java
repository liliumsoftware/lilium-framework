package ir.baho.framework.repository;

import ir.baho.framework.domain.Entity;
import ir.baho.framework.metadata.Metadata;
import ir.baho.framework.metadata.PageMetadata;
import ir.baho.framework.metadata.ProjectionMetadata;
import ir.baho.framework.metadata.ProjectionPageMetadata;
import ir.baho.framework.metadata.ReportMetadata;
import ir.baho.framework.metadata.SummaryPage;
import ir.baho.framework.metadata.report.ReportDesign;
import ir.baho.framework.repository.specification.PredicateCriteriaSpecification;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Attribute;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.hibernate.query.sqm.SqmPathSource;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@NoRepositoryBean
public interface JpaCriteriaRepository<E extends Entity<?, ID>, ID extends Serializable & Comparable<ID>>
        extends ReportRepository<E, ID>, JpaSpecificationExecutor<E> {

    char SEPARATOR = '.';

    @SafeVarargs
    static <T> boolean containsAttribute(Metadata metadata, Path<T> path, Attribute<T, ?>... attributes) {
        if (metadata == null) {
            return false;
        }
        List<String> fields = Stream.of(attributes).map(a -> getExpressionName(path) + SEPARATOR + a.getName()).toList();
        if (metadata instanceof ProjectionMetadata projectionMetadata && projectionMetadata.getField() != null &&
            fields.stream().anyMatch(f -> Stream.of(projectionMetadata.getField()).anyMatch(s -> s.equals(f) || s.startsWith(f + SEPARATOR)))) {
            return true;
        }
        if (metadata.getSearch() == null && metadata.getSort() == null) {
            return false;
        }
        boolean containsSearch = metadata.getSearch() != null &&
                                 fields.stream().anyMatch(f -> Stream.of(metadata.getSearch()).anyMatch(s -> s.getField().equals(f) || s.getField().startsWith(f + SEPARATOR)));
        boolean containsSort = metadata.getSort() != null &&
                               fields.stream().anyMatch(f -> Stream.of(metadata.getSort()).anyMatch(s -> s.getField().equals(f) || s.getField().startsWith(f + SEPARATOR)));
        return containsSearch || containsSort;
    }

    static String getExpressionName(Expression<?> expression) {
        if (expression instanceof Path<?> path) {
            StringBuilder sb = new StringBuilder();
            sb.append(((SqmPathSource<?>) path.getModel()).getPathName());
            while ((path = path.getParentPath()) instanceof Join<?, ?>) {
                sb.insert(0, ((Join<?, ?>) path).getAttribute().getName() + SEPARATOR);
            }
            return sb.toString();
        }
        return expression.getAlias();
    }

    void detach(E entity);

    E findOne(Metadata metadata, PredicateCriteriaSpecification<E> specification);

    Page<E> findAll(PageMetadata metadata, PredicateCriteriaSpecification<E> specification);

    <S> SummaryPage<E, S> findAll(PageMetadata metadata, PredicateCriteriaSpecification<E> specification, Class<S> summaryClass);

    List<E> findAll(Metadata metadata, PredicateCriteriaSpecification<E> specification);

    <P> P findOne(Metadata metadata, Class<P> projection, PredicateCriteriaSpecification<E> specification);

    <P> Page<P> findAll(PageMetadata metadata, Class<P> projection, PredicateCriteriaSpecification<E> specification);

    <P> List<P> findAll(Metadata metadata, Class<P> projection, PredicateCriteriaSpecification<E> specification);

    Map<String, Object> findOne(ProjectionMetadata metadata, PredicateCriteriaSpecification<E> specification);

    Page<Map<String, Object>> findAll(ProjectionPageMetadata metadata, PredicateCriteriaSpecification<E> specification);

    List<Map<String, Object>> findAll(ProjectionMetadata metadata, PredicateCriteriaSpecification<E> specification);

    JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, PredicateCriteriaSpecification<E> specification);

    JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, InputStream inputStream, PredicateCriteriaSpecification<E> specification);

}
