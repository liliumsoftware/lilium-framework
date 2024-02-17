package ir.baho.framework.domain;

import ir.baho.framework.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.RevisionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRevisionListener implements RevisionListener {

    private final CurrentUser currentUser;

    @Override
    public void newRevision(Object revisionEntity) {
        BaseRevisionEntity<?, ?> revisionInformation = (BaseRevisionEntity<?, ?>) revisionEntity;
        revisionInformation.setUsername(currentUser.username());
    }

}
