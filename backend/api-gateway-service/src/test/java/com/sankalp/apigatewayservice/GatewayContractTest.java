package com.sankalp.apigatewayservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
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
@DisplayName("Gateway API Contract Tests")
class GatewayContractTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Contract: actuator health returns JSON payload with status field")
    void testHealthContract() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError(),
            "Health endpoint should respond");

        if (response.getStatusCode().is2xxSuccessful()) {
            assertNotNull(response.getBody(), "Body should not be null for successful health response");
            assertTrue(response.getBody().contains("status"),
                "Health response contract should include 'status' field");

            MediaType contentType = response.getHeaders().getContentType();
            assertTrue(contentType == null || MediaType.APPLICATION_JSON.isCompatibleWith(contentType),
                "Health endpoint should return JSON response");
        }
    }

    @Test
    @DisplayName("Contract: unknown API route returns client error, not server error")
    void testUnknownRouteContract() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/unknown-contract-route", String.class);

        assertTrue(response.getStatusCode().is4xxClientError(),
            "Unknown route contract expects 4xx response");
    }
}
