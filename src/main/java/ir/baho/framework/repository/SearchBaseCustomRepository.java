package ir.baho.framework.repository;

import ir.baho.framework.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import lombok.SneakyThrows;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.engine.search.query.dsl.SearchQuerySelectStep;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SearchBaseCustomRepository {

    @Autowired
    protected EntityManager entityManager;

    @SneakyThrows
    protected void index(Class<?>... classes) {
        Search.session(entityManager).massIndexer(classes).startAndWait();
    }

    protected String path(Attribute<?, ?>... attributes) {
        return Arrays.stream(attributes).map(Attribute::getName).collect(Collectors.joining("."));
    }

    protected Optional<String> first(Object list) {
        if (list instanceof List<?> s && !s.isEmpty()) {
            Object first = s.getFirst();
            return Optional.ofNullable(first).map(Object::toString);
        }
        return Optional.empty();
    }

    protected final <E> SearchQuerySelectStep<?, ?, ?, E, SearchLoadingOptionsStep, ?, ?> getSearch(List<? extends Class<? extends E>> classes) {
        return Search.session(this.entityManager).search(classes);
    }

    @SafeVarargs
    protected final <E> SearchQuerySelectStep<?, ?, ?, E, SearchLoadingOptionsStep, ?, ?> getSearch(Class<? extends E>... classes) {
        return Search.session(this.entityManager).search(Arrays.stream(classes).toList());
    }

    protected final SearchScope<?> getScope(List<? extends Class<?>> classes) {
        return Search.session(this.entityManager).scope(classes);
    }

    @SafeVarargs
    protected final <E> SearchScope<?> getScope(Class<? extends E>... classes) {
        return Search.session(this.entityManager).scope(Arrays.stream(classes).toList());
    }

    protected <E> E getOne(SearchQueryOptionsStep<?, ?, E, SearchLoadingOptionsStep, ?, ?> query) {
        return query.fetchSingleHit().orElseThrow(() -> new NotFoundException("Search result not found"));
    }

    protected <E> E getOne(SearchQueryOptionsStep<?, ?, List<?>, SearchLoadingOptionsStep, ?, ?> query, Converter<List<?>, E> converter) {
        return converter.convert(query.fetchSingleHit().orElseThrow(() -> new NotFoundException("Search result not found")));
    }

    protected <E> List<E> getList(SearchQueryOptionsStep<?, ?, E, SearchLoadingOptionsStep, ?, ?> query) {
        return query.fetchAllHits();
    }

    protected <E> List<E> getList(SearchQueryOptionsStep<?, ?, List<?>, SearchLoadingOptionsStep, ?, ?> query, Converter<List<?>, E> converter) {
        return query.fetchAllHits().stream().map(converter::convert).collect(Collectors.toList());
    }

    protected <E> Page<E> getPage(SearchQueryOptionsStep<?, ?, E, SearchLoadingOptionsStep, ?, ?> query, Pageable pageable) {
        SearchResult<E> searchResult = query.fetch((int) pageable.getOffset(), pageable.getPageSize());
        List<E> entities = searchResult.hits();
        return PageableExecutionUtils.getPage(entities, pageable, () -> searchResult.total().hitCount());
    }

    protected <E> Page<E> getPage(SearchQueryOptionsStep<?, ?, List<?>, SearchLoadingOptionsStep, ?, ?> query, Converter<List<?>, E> converter, Pageable pageable) {
        SearchResult<List<?>> searchResult = query.fetch((int) pageable.getOffset(), pageable.getPageSize());
        List<E> entities = searchResult.hits().stream().map(converter::convert).collect(Collectors.toList());
        return PageableExecutionUtils.getPage(entities, pageable, () -> searchResult.total().hitCount());
    }

}
