package com.taskmanager.task.domain;

/**
 * Enumeration of task priority levels with numerical weights.
 * Conforms to DOC-BA-007.
 */
public enum TaskPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4);

    private final int weight;

    TaskPriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}

