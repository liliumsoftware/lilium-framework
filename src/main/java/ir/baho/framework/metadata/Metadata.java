package ir.baho.framework.metadata;

import ir.baho.framework.report.DateTimeFormatters;
import ir.baho.framework.service.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

@NoArgsConstructor
public class Metadata implements SortMetadata, Serializable {

    @Setter
    @Getter
    private CurrentUser currentUser;

    @Getter
    @Valid
    private Sort[] sort;

    @Getter
    @Valid
    private Search[] search;

    @Setter
    @Getter
    private boolean and = true;

    @Getter
    private transient boolean report = false;

    private transient boolean convert = false;

    public Metadata(@Valid Sort... sort) {
        this.sort = sort;
    }

    public Metadata(@Valid Search... search) {
        this.search = search;
    }

    public void setSort(Sort... sort) {
        this.sort = sort;
    }

    public void addSort(Sort... sort) {
        if (sort == null || sort.length == 0) {
            return;
        }
        List<Sort> sorts = this.sort == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(this.sort));
        sorts.addAll(Arrays.asList(sort));
        this.sort = sorts.toArray(Sort[]::new);
    }

    public void setSearch(Search... search) {
        this.search = search;
    }

    public void addSearch(Search... search) {
        if (search == null || search.length == 0) {
            return;
        }
        List<Search> searches = this.search == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(this.search));
        searches.addAll(Arrays.asList(search));
        this.search = searches.toArray(Search[]::new);
    }

    public void report() {
        this.report = true;
    }

    public void converted() {
        this.convert = true;
    }

    public boolean notConverted() {
        return !convert;
    }

    public String getUsername() {
        return currentUser.username();
    }

    public Locale getLocale() {
        return currentUser.locale();
    }

    public boolean isRtl() {
        return currentUser.isRtl();
    }

    public DateTimeFormatters getDateTimeFormatters() {
        return new DateTimeFormatters(currentUser.zoneId(), currentUser.calendarType(),
                currentUser.dateFormat(), currentUser.dateTimeFormat(),
                currentUser.timeFormat(), currentUser.durationType());
    }

    public void renameField(String from, String to) {
        if (sort != null) {
            Stream.of(sort).filter(s -> Objects.equals(s.getField(), from)).forEach(s -> s.setField(to));
        }
        if (search != null) {
            Stream.of(search).filter(s -> Objects.equals(s.getField(), from)).forEach(s -> s.setField(to));
        }
    }

    @AssertTrue(message = "{ir.baho.framework.search.Constraint}")
    private boolean isSearchConstraintValid() {
        return search == null || Stream.of(search).noneMatch(s -> s.getConstraint() == Constraint.UNKNOWN);
    }

}
