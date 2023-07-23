package ir.baho.framework.repository.impl;

import ir.baho.framework.domain.BaseRevisionEntity;
import org.springframework.data.repository.history.support.RevisionEntityInformation;

public class DefaultRevisionEntityInformation implements RevisionEntityInformation {

    public Class<?> getRevisionNumberType() {
        return Long.class;
    }

    public boolean isDefaultRevisionEntity() {
        return true;
    }

    public Class<?> getRevisionEntityClass() {
        return BaseRevisionEntity.class;
    }

}
