package com.taskmanager.notification.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaConsumerConfigTest {

    @Test
    @DisplayName("Should create DefaultErrorHandler with DeadLetterPublishingRecoverer targeting .DLT topic")
    void testErrorHandlerConfig() {
        @SuppressWarnings("unchecked")
        KafkaOperations<Object, Object> kafkaOperations = mock(KafkaOperations.class);

        KafkaConsumerConfig config = new KafkaConsumerConfig();
        DefaultErrorHandler errorHandler = config.errorHandler(kafkaOperations);

        assertThat(errorHandler).isNotNull();
        assertThat(errorHandler.isAckAfterHandle()).isTrue();
    }

    @Test
    @DisplayName("Should route failing records to DLT topic with .DLT suffix")
    void testDeadLetterRecovererTopicResolution() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ConsumerRecord<String, String> record = new ConsumerRecord<>("task-events", 1, 10L, "key", "val");
        TopicPartition destination = config.destinationResolver().apply(record, new RuntimeException("Fatal error"));

        assertThat(destination.topic()).isEqualTo("task-events.DLT");
        assertThat(destination.partition()).isEqualTo(1);
    }
}

