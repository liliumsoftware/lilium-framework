package ir.baho.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityMetadata<ID extends Serializable & Comparable<ID>> implements Serializable {

    @Id
    private ID id;
    private LocalDateTime lastModifiedDate;
    private int version;

}
