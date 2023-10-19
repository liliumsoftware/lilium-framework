package ir.baho.framework.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import ir.baho.framework.exception.TransparentClientException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.io.IOException;

@ConditionalOnClass(name = "feign.codec.ErrorDecoder")
@AutoConfiguration
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() < 500) {
            try {
                return new TransparentClientException(response.status(), new String(response.body().asInputStream().readAllBytes()));
            } catch (IOException ignored) {
            }
        }
        return errorDecoder.decode(methodKey, response);
    }

}
