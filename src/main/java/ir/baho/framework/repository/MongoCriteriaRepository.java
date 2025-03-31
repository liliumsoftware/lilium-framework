package ir.baho.framework.repository;

import ir.baho.framework.domain.Entity;
import ir.baho.framework.metadata.Metadata;
import ir.baho.framework.metadata.PageMetadata;
import ir.baho.framework.metadata.ProjectionMetadata;
import ir.baho.framework.metadata.ProjectionPageMetadata;
import ir.baho.framework.metadata.ReportMetadata;
import ir.baho.framework.metadata.report.ReportDesign;
import ir.baho.framework.repository.specification.PredicateAggregateSpecification;
import ir.baho.framework.repository.specification.PredicateMongoSpecification;
import ir.baho.framework.repository.specification.SimpleSelections;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@NoRepositoryBean
public interface MongoCriteriaRepository<E extends Entity<?, ID>, ID extends Serializable & Comparable<ID>>
        extends ReportRepository<E, ID>, MongoRepository<E, ID> {

    E findOne(Metadata metadata, PredicateMongoSpecification specification);

    Page<E> findAll(PageMetadata metadata, PredicateMongoSpecification specification);

    List<E> findAll(Metadata metadata, PredicateMongoSpecification specification);

    <P> P findOne(Metadata metadata, Class<P> projection, PredicateMongoSpecification specification, SimpleSelections selections);

    <P> Page<P> findAll(PageMetadata metadata, Class<P> projection, PredicateMongoSpecification specification, SimpleSelections selections);

    <P> List<P> findAll(Metadata metadata, Class<P> projection, PredicateMongoSpecification specification, SimpleSelections selections);

    Map<String, Object> findOne(ProjectionMetadata metadata, PredicateMongoSpecification specification, SimpleSelections selections);

    Page<Map<String, Object>> findAll(ProjectionPageMetadata metadata, PredicateMongoSpecification specification, SimpleSelections selections);

    List<Map<String, Object>> findAll(ProjectionMetadata metadata, PredicateMongoSpecification specification, SimpleSelections selections);

    <P> P findOne(Metadata metadata, Class<P> projection, PredicateAggregateSpecification specification);

    <P> Page<P> findAll(PageMetadata metadata, Class<P> projection, PredicateAggregateSpecification specification);

    <P> List<P> findAll(Metadata metadata, Class<P> projection, PredicateAggregateSpecification specification);

    JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, PredicateMongoSpecification specification, SimpleSelections selections);

    JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, InputStream inputStream, PredicateMongoSpecification specification, SimpleSelections selections);

    JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, Class<?> projection, PredicateAggregateSpecification specification, SimpleSelections selections);

    JasperReportBuilder report(ReportMetadata metadata, ReportDesign design, Class<?> projection, InputStream inputStream, PredicateAggregateSpecification specification, SimpleSelections selections);

}
