package ir.baho.framework.audit;

import ir.baho.framework.domain.Version;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.springframework.data.history.RevisionMetadata;

import java.util.Map;

public class RevisionTypePropertyProvider implements CommitPropertiesProvider {

    private static final String KEY = RevisionMetadata.RevisionType.class.getSimpleName();

    @Override
    public Map<String, String> provideForCommittedObject(Object domainObject) {
        if (domainObject instanceof Version version) {
            return Map.of(KEY, version.getVersion() == 0 ?
                    RevisionMetadata.RevisionType.INSERT.name() : RevisionMetadata.RevisionType.UPDATE.name());
        }
        return CommitPropertiesProvider.super.provideForCommittedObject(domainObject);
    }

    @Override
    public Map<String, String> provideForDeletedObject(Object domainObject) {
        return Map.of(KEY, RevisionMetadata.RevisionType.DELETE.name());
    }

    @Override
    public Map<String, String> provideForDeleteById(Class<?> domainObjectClass, Object domainObjectId) {
        return Map.of(KEY, RevisionMetadata.RevisionType.DELETE.name());
    }

}
