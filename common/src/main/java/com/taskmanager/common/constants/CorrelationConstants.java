package com.taskmanager.common.constants;

/**
 * Constants governing distributed correlation and tracing across HTTP and Kafka boundaries.
 */
public final class CorrelationConstants {

    /**
     * Standard HTTP request and response header key for distributed correlation ID propagation.
     */
    public static final String HEADER_NAME = "X-Correlation-ID";

    /**
     * SLF4J Mapped Diagnostic Context (MDC) key used in Logback patterns across all services.
     */
    public static final String MDC_KEY = "correlationId";

    /**
     * Kafka message header key for correlating asynchronous event streams.
     */
    public static final String KAFKA_HEADER_NAME = "correlationId";

    private CorrelationConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}

