package ir.baho.framework.web;

import ir.baho.framework.exception.TransparentClientException;
import ir.baho.framework.i18n.MessageResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public abstract class BaseFallbackFactory<T> implements FallbackFactory<T> {

    @Autowired
    protected MessageResource messageResource;

    @Override
    public T create(Throwable cause) {
        if (cause instanceof TransparentClientException e) {
            throw e;
        }
        log.warn("An exception occurred when calling the Feign client", cause);
        return createClient(cause);
    }

    protected abstract T createClient(Throwable cause);

}
