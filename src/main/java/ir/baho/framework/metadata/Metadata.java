package ir.baho.framework.metadata;

import ir.baho.framework.metadata.report.DateTimeFormatters;
import ir.baho.framework.service.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Locale;
import java.util.stream.Stream;

@NoArgsConstructor
public class Metadata implements SortMetadata, Serializable {

    private CurrentUser currentUser;

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

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
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

    public String getUsername() {
        return currentUser.getUsername();
    }

    public Locale getLocale() {
        return currentUser.getLocale();
    }

    public boolean isRtl() {
        return currentUser.isRtl();
    }

    public DateTimeFormatters getDateTimeFormatters() {
        return new DateTimeFormatters(currentUser.getLocale(),
                currentUser.getTimeZone().toZoneId(), currentUser.getCalendarType(),
                currentUser.getDateFormat(), currentUser.getDateTimeFormat(),
                currentUser.getTimeFormat(), currentUser.getDurationType());
    }

    @AssertTrue(message = "{ir.baho.framework.search.Constraint}")
    private boolean isSearchConstraintValid() {
        return search == null || Stream.of(search).noneMatch(s -> s.getConstraint() == Constraint.UNKNOWN);
    }

}
