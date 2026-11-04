package com.taskmanager.notification.exception;

/**
 * Exception thrown when an asynchronous Kafka event fails during processing.
 */
public class EventProcessingException extends RuntimeException {

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

