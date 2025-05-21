package ir.baho.framework.repository;

import ir.baho.framework.domain.BaseEntitySimple;
import ir.baho.framework.dto.EntityMetadata;
import ir.baho.framework.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

@NoRepositoryBean
public interface JpaMetadataRepository<E extends BaseEntitySimple<?, ID>, ID extends Serializable & Comparable<ID>> extends JpaRepository<E, ID> {

    default E findOne(ID id) {
        return findById(id).orElseThrow(() -> new NotFoundException("FindOne: " + id));
    }

    default void deleteOne(ID id) {
        if (existsById(id)) {
            deleteById(id);
        } else {
            throw new NotFoundException("DeleteOne: " + id);
        }
    }

    Optional<EntityMetadataProjection<ID>> findMetadataProjectedById(ID id);

    default EntityMetadata<ID> findMetadata(ID id) {
        EntityMetadataProjection<ID> entityMetadata = findMetadataProjectedById(id).orElseThrow(() -> new NotFoundException("FindMetadata: " + id));
        return new EntityMetadata<>(entityMetadata.getId(), entityMetadata.getLastModifiedDate(), entityMetadata.getVersion());
    }

    boolean existsBy();

    interface EntityMetadataProjection<ID extends Serializable & Comparable<ID>> {
        ID getId();

        LocalDateTime getLastModifiedDate();

        int getVersion();
    }

}
