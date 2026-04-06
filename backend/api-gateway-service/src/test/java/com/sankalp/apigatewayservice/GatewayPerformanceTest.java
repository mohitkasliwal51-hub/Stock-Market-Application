package com.sankalp.apigatewayservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.cloud.loadbalancer.eager-loading.enabled=true"
})
@DisplayName("Gateway Performance Baseline Tests")
class GatewayPerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Performance: health endpoint responds under 2 seconds")
    void testHealthEndpointLatency() {
        long start = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError(),
            "Health endpoint should respond");
        assertTrue(elapsed < 2000, "Health endpoint should respond in under 2000ms, actual: " + elapsed + "ms");
    }

    @Test
    @DisplayName("Performance: average latency for repeated health checks under 1 second")
    void testAverageHealthLatency() {
        long total = 0;
        int requests = 5;

        for (int i = 0; i < requests; i++) {
            long start = System.currentTimeMillis();
            restTemplate.getForEntity("/actuator/health", String.class);
            total += (System.currentTimeMillis() - start);
        }

        long average = total / requests;
        assertTrue(average < 1000, "Average health latency should be under 1000ms, actual: " + average + "ms");
    }
}
