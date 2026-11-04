package com.taskmanager.task.domain;

import com.taskmanager.task.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskStateMachineTest {

    @ParameterizedTest
    @CsvSource({
            "TODO, IN_PROGRESS",
            "TODO, COMPLETED",
            "TODO, CANCELLED",
            "TODO, TODO",
            "IN_PROGRESS, TODO",
            "IN_PROGRESS, COMPLETED",
            "IN_PROGRESS, CANCELLED",
            "IN_PROGRESS, IN_PROGRESS",
            "COMPLETED, TODO",
            "COMPLETED, IN_PROGRESS",
            "COMPLETED, COMPLETED",
            "CANCELLED, TODO",
            "CANCELLED, CANCELLED"
    })
    @DisplayName("Should allow documented valid state transitions")
    void testAllowedTransitions(TaskStatus source, TaskStatus target) {
        assertThatCode(() -> TaskStateMachine.validateTransition(source, target))
                .doesNotThrowAnyException();

        assertThat(TaskStateMachine.isValidTransition(source, target)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "COMPLETED, CANCELLED",
            "CANCELLED, IN_PROGRESS",
            "CANCELLED, COMPLETED"
    })
    @DisplayName("Should prohibit documented invalid state transitions")
    void testProhibitedTransitions(TaskStatus source, TaskStatus target) {
        assertThat(TaskStateMachine.isValidTransition(source, target)).isFalse();

        assertThatThrownBy(() -> TaskStateMachine.validateTransition(source, target))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Cannot transition task from " + source + " to " + target);
    }

    @Test
    @DisplayName("Should reject null source or target status")
    void testNullStatusTransition() {
        assertThatThrownBy(() -> TaskStateMachine.validateTransition(null, TaskStatus.TODO))
                .isInstanceOf(InvalidStatusTransitionException.class);

        assertThatThrownBy(() -> TaskStateMachine.validateTransition(TaskStatus.TODO, null))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("Should validate initial creation status")
    void testCreationStatus() {
        assertThat(TaskStateMachine.isValidCreationStatus(TaskStatus.TODO)).isTrue();
        assertThat(TaskStateMachine.isValidCreationStatus(TaskStatus.IN_PROGRESS)).isTrue();
        assertThat(TaskStateMachine.isValidCreationStatus(TaskStatus.COMPLETED)).isFalse();
        assertThat(TaskStateMachine.isValidCreationStatus(TaskStatus.CANCELLED)).isFalse();
        assertThat(TaskStateMachine.isValidCreationStatus(null)).isFalse();
    }
}

