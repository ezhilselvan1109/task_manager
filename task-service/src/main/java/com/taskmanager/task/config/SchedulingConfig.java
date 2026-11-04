package com.taskmanager.task.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring task scheduling for periodic outbox table polling.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

