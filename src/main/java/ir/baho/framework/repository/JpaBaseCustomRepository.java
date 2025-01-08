package ir.baho.framework.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public abstract class JpaBaseCustomRepository {

    @Autowired
    protected EntityManager entityManager;

    protected <E, P> Page<P> getPage(Root<E> root, CriteriaQuery<P> criteriaQuery, CriteriaBuilder criteriaBuilder,
                                     Pageable pageable, Specification<E> specification) {
        long total = getTotal(root, criteriaBuilder, specification);
        criteriaQuery.where(specification.toPredicate(root, criteriaQuery, criteriaBuilder));
        if (pageable.getSort().isSorted()) {
            criteriaQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        return getPage(criteriaQuery, total, pageable);
    }

    protected <E, P> List<P> getList(Root<E> root, CriteriaQuery<P> criteriaQuery, CriteriaBuilder criteriaBuilder,
                                     Sort sort, Specification<E> specification) {
        criteriaQuery.where(specification.toPredicate(root, criteriaQuery, criteriaBuilder));
        if (sort != null && sort.isSorted()) {
            criteriaQuery.orderBy(QueryUtils.toOrders(sort, root, criteriaBuilder));
        }
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    protected <E, P> Page<P> getPage(Root<E> root, CriteriaQuery<P> criteriaQuery, CriteriaBuilder criteriaBuilder,
                                     Pageable pageable, Specification<E> specification, SpecificationConsumer<E, P> consumer) {
        long total = getTotal(root, criteriaBuilder, specification);
        criteriaQuery.where(specification.toPredicate(root, criteriaQuery, criteriaBuilder));
        consumer.accept(root, criteriaQuery, criteriaBuilder);
        if (pageable.getSort().isSorted()) {
            criteriaQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        return getPage(criteriaQuery, total, pageable);
    }

    protected <E, P> List<P> getList(Root<E> root, CriteriaQuery<P> criteriaQuery, CriteriaBuilder criteriaBuilder,
                                     Sort sort, Specification<E> specification, SpecificationConsumer<E, P> consumer) {
        criteriaQuery.where(specification.toPredicate(root, criteriaQuery, criteriaBuilder));
        consumer.accept(root, criteriaQuery, criteriaBuilder);
        if (sort != null && sort.isSorted()) {
            criteriaQuery.orderBy(QueryUtils.toOrders(sort, root, criteriaBuilder));
        }
        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    protected <E> Page<E> getPage(CriteriaQuery<E> criteriaQuery, long total, Pageable pageable) {
        return PageableExecutionUtils.getPage(entityManager.createQuery(criteriaQuery)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList(), pageable, () -> total);
    }

    @SuppressWarnings("unchecked")
    protected <E, P> long getTotal(Root<E> root, CriteriaBuilder criteriaBuilder, Specification<E> specification) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<E> countRoot = (Root<E>) countQuery.from(root.getJavaType());
        countQuery.where(specification.toPredicate(countRoot, countQuery, criteriaBuilder));
        countQuery.select(criteriaBuilder.countDistinct(countRoot));
        return executeCountQuery(entityManager.createQuery(countQuery));
    }

    protected long executeCountQuery(TypedQuery<Long> query) {
        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }
        return total;
    }

    @FunctionalInterface
    public interface SpecificationConsumer<E, P> {
        void accept(Root<E> r, CriteriaQuery<P> cq, CriteriaBuilder cb);
    }

}
