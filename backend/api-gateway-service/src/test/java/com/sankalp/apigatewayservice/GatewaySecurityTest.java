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
@DisplayName("Gateway Security Tests")
class GatewaySecurityTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Security: sensitive actuator endpoint is not publicly open")
    void testSensitiveActuatorEndpointNotOpen() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/env", String.class);

        assertTrue(
            response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError(),
            "Sensitive actuator endpoint should not be publicly accessible"
        );
    }

    @Test
    @DisplayName("Security: unknown route does not expose stack traces")
    void testUnknownRouteDoesNotLeakDetails() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/security-nonexistent-route", String.class);

        assertTrue(response.getStatusCode().is4xxClientError(),
            "Unknown route should return 4xx");

        String body = response.getBody();
        assertTrue(body == null || !body.toLowerCase().contains("exception"),
            "Error responses should not leak exception details");
    }
}
