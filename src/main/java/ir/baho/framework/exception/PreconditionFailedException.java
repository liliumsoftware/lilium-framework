package ir.baho.framework.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PreconditionFailedException extends RuntimeException {

    private final int version;

}
