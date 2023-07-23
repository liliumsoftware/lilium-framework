package ir.baho.framework.metadata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.history.RevisionMetadata;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class EnversMetadata extends Metadata {

    private String username;

    private LocalDateTime from;

    private LocalDateTime to;

    private String[] changedProperty;

    @NotNull
    @Size(min = 1, max = 3)
    private RevisionMetadata.RevisionType[] revTypes = {
            RevisionMetadata.RevisionType.INSERT,
            RevisionMetadata.RevisionType.UPDATE,
            RevisionMetadata.RevisionType.DELETE};

    public EnversMetadata(@Valid Sort... sort) {
        super(sort);
    }

    public EnversMetadata(@Valid Search... search) {
        super(search);
    }

}
