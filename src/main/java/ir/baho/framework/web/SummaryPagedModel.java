package ir.baho.framework.web;

import lombok.Getter;
import org.springframework.hateoas.PagedModel;

@Getter
public class SummaryPagedModel<D, S> extends PagedModel<D> {

    private final S summary;

    public SummaryPagedModel(PagedModel<D> pagedModel, S summary) {
        super(pagedModel.getContent(), pagedModel.getMetadata(), pagedModel.getLinks(), pagedModel.getResolvableType());
        this.summary = summary;
    }

}
