package ir.baho.framework.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
@Getter
public class Error implements Serializable {

    private final String defaultMessage;

}
