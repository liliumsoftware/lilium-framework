package ir.baho.framework.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.NotAudited;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@MappedSuperclass
public abstract class BaseEntity<E extends BaseEntity<E, ID>, ID extends Serializable & Comparable<ID>> extends BaseEntitySimple<E, ID> {

    @DiffIgnore
    @NotAudited
    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    @Size(max = 100)
    private String createdBy;

    @DiffIgnore
    @NotAudited
    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @DiffIgnore
    @NotAudited
    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY", nullable = false)
    @Size(max = 100)
    private String lastModifiedBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

}
