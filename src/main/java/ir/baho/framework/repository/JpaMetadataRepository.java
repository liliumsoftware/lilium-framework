package ir.baho.framework.repository;

import ir.baho.framework.domain.BaseEntitySimple;
import ir.baho.framework.dto.EntityMetadata;
import ir.baho.framework.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

@NoRepositoryBean
public interface JpaMetadataRepository<E extends BaseEntitySimple<?, ID>, ID extends Serializable & Comparable<ID>> extends JpaRepository<E, ID> {

    default E findOne(ID id) {
        return findById(id).orElseThrow(NotFoundException::new);
    }

    Optional<EntityMetadata<ID>> findMetadataById(ID id);

    default EntityMetadata<ID> findMetadata(ID id) {
        return findMetadataById(id).orElseThrow(NotFoundException::new);
    }

    boolean existsBy();

}
