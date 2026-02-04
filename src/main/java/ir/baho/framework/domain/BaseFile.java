package ir.baho.framework.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@SuperBuilder(toBuilder = true)
@MappedSuperclass
public abstract class BaseFile<E extends BaseFile<E, ID>, ID extends Serializable & Comparable<ID>> extends BaseEntitySimple<E, ID> {

    @NotBlank
    @Size(max = 100)
    @Column(name = "NAME")
    private String name;

    @NotBlank
    @Size(max = 100)
    @Column(name = "FILE_TYPE")
    private String type;

    @Column(name = "FILE_SIZE")
    private int size;

    @Lob
    @Column(name = "VALUE")
    private byte[] value;

}
