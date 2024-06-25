package ir.baho.framework.metadata;

import ir.baho.framework.metadata.report.DateTimeFormatters;
import ir.baho.framework.service.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
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

    public void setSearch(Search... search) {
        this.search = search;
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
