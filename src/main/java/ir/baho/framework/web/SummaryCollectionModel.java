package ir.baho.framework.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.CollectionModel;

@NoArgsConstructor
@Getter
@Setter
public class SummaryCollectionModel<D, S> extends CollectionModel<D> {

    private S summary;

    public SummaryCollectionModel(CollectionModel<D> collectionModel) {
        super(collectionModel.getContent(), collectionModel.getLinks(), collectionModel.getResolvableType());
    }

    public SummaryCollectionModel(CollectionModel<D> collectionModel, S summary) {
        super(collectionModel.getContent(), collectionModel.getLinks(), collectionModel.getResolvableType());
        this.summary = summary;
    }

}
