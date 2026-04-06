package com.sankalp.apigatewayservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.cloud.loadbalancer.eager-loading.enabled=true"
})
@DisplayName("Gateway Smoke Tests")
class GatewaySmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Smoke: application health endpoint responds")
    void testHealthEndpointResponds() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError(),
            "Health endpoint should respond without server crash");
    }

    @Test
    @DisplayName("Smoke: gateway route for companies is reachable")
    void testCompaniesRouteReachable() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/companies", String.class);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().value() != 404,
            "Known company route should not return 404");
    }

    @Test
    @DisplayName("Smoke: gateway route for sectors is reachable")
    void testSectorsRouteReachable() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/sectors", String.class);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().value() != 404,
            "Known sector route should not return 404");
    }
}
