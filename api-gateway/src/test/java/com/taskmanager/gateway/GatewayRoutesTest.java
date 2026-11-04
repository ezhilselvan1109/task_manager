package com.taskmanager.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.enabled=false"
})
class GatewayRoutesTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("Should load all configured routes for user-service, task-service, and notification-service")
    void testRoutesConfigured() {
        StepVerifier.create(routeLocator.getRoutes())
                .recordWith(java.util.ArrayList::new)
                .thenConsumeWhile(r -> true)
                .consumeRecordedWith(routes -> {
                    List<String> routeIds = routes.stream().map(r -> r.getId()).toList();
                    assertThat(routeIds).contains(
                            "user-auth-route",
                            "user-profile-route",
                            "task-service-route",
                            "notification-service-route"
                    );
                })
                .verifyComplete();
    }
}

