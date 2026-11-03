package com.taskmanager.common.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConstantsTest {

    @Test
    @DisplayName("CorrelationConstants values must match architectural baseline")
    void testCorrelationConstants() {
        assertThat(CorrelationConstants.HEADER_NAME).isEqualTo("X-Correlation-ID");
        assertThat(CorrelationConstants.MDC_KEY).isEqualTo("correlationId");
        assertThat(CorrelationConstants.KAFKA_HEADER_NAME).isEqualTo("correlationId");
    }

    @Test
    @DisplayName("SecurityConstants values must match architectural baseline")
    void testSecurityConstants() {
        assertThat(SecurityConstants.HEADER_USER_ID).isEqualTo("X-User-Id");
        assertThat(SecurityConstants.HEADER_USER_ROLE).isEqualTo("X-User-Role");
        assertThat(SecurityConstants.HEADER_USER_NAME).isEqualTo("X-User-Name");
        assertThat(SecurityConstants.AUTH_HEADER).isEqualTo("Authorization");
        assertThat(SecurityConstants.BEARER_PREFIX).isEqualTo("Bearer ");
        assertThat(SecurityConstants.ROLE_USER).isEqualTo("ROLE_USER");
        assertThat(SecurityConstants.ROLE_ADMIN).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("CorrelationConstants constructor throws UnsupportedOperationException")
    void testCorrelationConstantsConstructorReflection() throws NoSuchMethodException {
        Constructor<CorrelationConstants> constructor = CorrelationConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("SecurityConstants constructor throws UnsupportedOperationException")
    void testSecurityConstantsConstructorReflection() throws NoSuchMethodException {
        Constructor<SecurityConstants> constructor = SecurityConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}

