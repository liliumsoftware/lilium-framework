package ir.baho.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EntityMetadata<ID extends Serializable & Comparable<ID>> implements Serializable {

    @Id
    private ID id;
    private LocalDateTime lastModifiedDate;
    private int version;

}
