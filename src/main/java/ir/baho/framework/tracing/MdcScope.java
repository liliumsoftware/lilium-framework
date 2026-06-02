package ir.baho.framework.tracing;

import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Scoped MDC mutation. Use with try-with-resources so the original MDC state is
 * always restored, even if the body throws.
 *
 * <pre>{@code
 *   try (MdcScope ignored = MdcScope.forJob("reconciler.verify")) {
 *       ...
 *   }
 * }</pre>
 */
public final class MdcScope implements AutoCloseable {

    private final Map<String, String> previous = new LinkedHashMap<>();

    private MdcScope(Map<String, String> additions) {
        for (Map.Entry<String, String> e : additions.entrySet()) {
            previous.put(e.getKey(), MDC.get(e.getKey()));
            if (e.getValue() == null) {
                MDC.remove(e.getKey());
            } else {
                MDC.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Generates a fresh request id and tags the MDC with a job name.
     */
    public static MdcScope forJob(String jobName) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(TraceConstants.REQUEST_ID_MDC_KEY, UUID.randomUUID().toString());
        m.put(TraceConstants.JOB_MDC_KEY, jobName);
        return new MdcScope(m);
    }

    /**
     * Adds (or overrides) a single MDC key.
     */
    public static MdcScope of(String key, String value) {
        return new MdcScope(Map.of(key, value));
    }

    @Override
    public void close() {
        for (Map.Entry<String, String> e : previous.entrySet()) {
            if (e.getValue() == null) {
                MDC.remove(e.getKey());
            } else {
                MDC.put(e.getKey(), e.getValue());
            }
        }
    }

}
