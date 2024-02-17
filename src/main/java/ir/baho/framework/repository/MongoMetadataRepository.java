package ir.baho.framework.repository;

import ir.baho.framework.domain.BaseDocumentSimple;
import ir.baho.framework.dto.EntityMetadata;
import ir.baho.framework.exception.NotFoundException;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

@NoRepositoryBean
public interface MongoMetadataRepository<E extends BaseDocumentSimple<?, ID>, ID extends Serializable & Comparable<ID>> extends MongoRepository<E, ID> {

    default E findOne(ID id) {
        return findById(id).orElseThrow(NotFoundException::new);
    }

    Optional<EntityMetadataProjection<ID>> findMetadataProjectedById(ID id);

    default EntityMetadata<ID> findMetadata(ID id) {
        EntityMetadataProjection<ID> entityMetadata = findMetadataProjectedById(id).orElseThrow(NotFoundException::new);
        return new EntityMetadata<>(entityMetadata.getId(), entityMetadata.getLastModifiedDate(), entityMetadata.getVersion());
    }

    boolean existsBy();

    interface EntityMetadataProjection<ID extends Serializable & Comparable<ID>> {
        ID getId();

        LocalDateTime getLastModifiedDate();

        int getVersion();
    }

}
