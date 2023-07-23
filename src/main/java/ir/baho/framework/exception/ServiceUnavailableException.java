package ir.baho.framework.exception;

import lombok.Getter;

@Getter
public class ServiceUnavailableException extends RuntimeException {

    private final String service;

    public ServiceUnavailableException(String service) {
        super(service);
        this.service = service;
    }

}
