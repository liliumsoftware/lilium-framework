package ir.baho.framework.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.PagedModel;

@NoArgsConstructor
@Getter
@Setter
public class SummaryPagedModel<D, S> extends PagedModel<D> {

    private S summary;

    public SummaryPagedModel(PagedModel<D> pagedModel) {
        super(pagedModel.getContent(), pagedModel.getMetadata(), pagedModel.getLinks(), pagedModel.getResolvableType());
    }

    public SummaryPagedModel(PagedModel<D> pagedModel, S summary) {
        super(pagedModel.getContent(), pagedModel.getMetadata(), pagedModel.getLinks(), pagedModel.getResolvableType());
        this.summary = summary;
    }

}
