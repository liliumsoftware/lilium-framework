package ir.baho.framework.metadata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.stream.Stream;

@NoArgsConstructor
public class Metadata implements SortMetadata, Serializable {

    @Valid
    private Sort[] sort;

    @Valid
    private Search[] search;

    private boolean and = true;

    private transient boolean convert = false;

    public Metadata(@Valid Sort... sort) {
        this.sort = sort;
    }

    public Metadata(@Valid Search... search) {
        this.search = search;
    }

    public Sort[] getSort() {
        return sort;
    }

    public void setSort(Sort... sort) {
        this.sort = sort;
    }

    public Search[] getSearch() {
        return search;
    }

    public void setSearch(Search... search) {
        this.search = search;
    }

    public boolean isAnd() {
        return and;
    }

    public void setAnd(boolean and) {
        this.and = and;
    }

    public void converted() {
        this.convert = true;
    }

    public boolean notConverted() {
        return !convert;
    }

    @AssertTrue(message = "{ir.baho.framework.search.Constraint}")
    private boolean isSearchConstraintValid() {
        return search == null || Stream.of(search).noneMatch(s -> s.getConstraint() == Constraint.UNKNOWN);
    }

}
