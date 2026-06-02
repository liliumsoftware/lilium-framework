package ir.baho.framework.tracing;

import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Adds {@code x-request-id} to every outbound RabbitMQ message based on the current MDC.
 * Consumers can echo the value to keep correlation across service boundaries.
 *
 * <p>Auto-registered only when {@code spring-amqp} is on the consumer's classpath.
 */
@AutoConfiguration
@ConditionalOnClass(MessagePostProcessor.class)
public class RequestIdMessagePostProcessor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        String requestId = MDC.get(TraceConstants.REQUEST_ID_MDC_KEY);
        if (requestId != null && !requestId.isBlank()) {
            message.getMessageProperties().setHeader(TraceConstants.AMQP_REQUEST_ID_HEADER, requestId);
        }
        return message;
    }

}
