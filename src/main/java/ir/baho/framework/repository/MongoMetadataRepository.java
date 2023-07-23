package ir.baho.framework.repository;

import ir.baho.framework.domain.BaseDocumentSimple;
import ir.baho.framework.dto.EntityMetadata;
import ir.baho.framework.exception.NotFoundException;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

@NoRepositoryBean
public interface MongoMetadataRepository<E extends BaseDocumentSimple<?, ID>, ID extends Serializable & Comparable<ID>> extends MongoRepository<E, ID> {

    default E findOne(ID id) {
        return findById(id).orElseThrow(NotFoundException::new);
    }

    Optional<EntityMetadata<String>> findMetadataById(String id);

    default EntityMetadata<String> findMetadata(String id) {
        return findMetadataById(id).orElseThrow(NotFoundException::new);
    }

    boolean existsBy();

}
