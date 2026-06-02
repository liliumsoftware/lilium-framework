package ir.baho.framework.tracing;

public final class TraceConstants {

    /**
     * HTTP header carrying the correlation id, both inbound and outbound.
     */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * SLF4J MDC key. Surfaced in the log pattern via {@code %X{requestId}}.
     */
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    /**
     * AMQP message header carrying the correlation id on outbound RabbitMQ messages.
     */
    public static final String AMQP_REQUEST_ID_HEADER = "x-request-id";

    /**
     * Optional MDC key for scheduled-job names.
     */
    public static final String JOB_MDC_KEY = "job";

    private TraceConstants() {
    }

}
