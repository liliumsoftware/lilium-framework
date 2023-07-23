package ir.baho.framework.metadata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.history.RevisionMetadata;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class JaversMetadata implements Serializable {

    private String username;

    private LocalDateTime from;

    private LocalDateTime to;

    private String[] changedProperty;

    private RevisionMetadata.RevisionType revType;

}
