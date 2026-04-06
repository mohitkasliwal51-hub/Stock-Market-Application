package com.sankalp.apigatewayservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for API Gateway routing.
 * Tests that the gateway correctly routes requests to different microservices.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.cloud.loadbalancer.eager-loading.enabled=true"
})
class GatewayIntegrationTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String cachedAuthToken;

    private ResponseEntity<String> getWithRetry(String path) {
        return getWithRetry(path, null);
    }

    private ResponseEntity<String> getWithRetry(String path, HttpHeaders headers) {
        ResponseEntity<String> response = null;
        for (int i = 0; i < 8; i++) {
            if (headers == null) {
                response = restTemplate.getForEntity(BASE_URL + path, String.class);
            } else {
                response = restTemplate.exchange(
                    BASE_URL + path,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
                );
            }

            if (!response.getStatusCode().is5xxServerError() || i == 7) {
                return response;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for downstream services to become ready");
            }
        }

        return response;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());
        return headers;
    }

    private String getAuthToken() {
        if (cachedAuthToken != null && !cachedAuthToken.isBlank()) {
            return cachedAuthToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = "{\"username\":\"admin\",\"password\":\"admin123\"}";

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            BASE_URL + "/api/auth/login",
            new HttpEntity<>(payload, headers),
            String.class
        );

        assertTrue(loginResponse.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/auth/login, but got " + loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody(), "Login response body must not be null");

        try {
            JsonNode root = objectMapper.readTree(loginResponse.getBody());
            String token = root.path("data").path("token").asText();
            assertFalse(token == null || token.isBlank(), "JWT token missing in login response");
            cachedAuthToken = token;
            return token;
        } catch (Exception ex) {
            fail("Failed to parse login response for JWT token: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Test that gateway correctly routes company service requests
     */
    @Test
    void testGatewayRoutesToCompanyService() {
        ResponseEntity<String> response = getWithRetry("/api/companies", authHeaders());

        assertNotNull(response, "Gateway response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/companies when services are live, but got " + response.getStatusCode());
    }

    /**
     * Test that gateway correctly routes exchange service requests
     */
    @Test
    void testGatewayRoutesToExchangeService() {
        ResponseEntity<String> response = getWithRetry("/api/stockExchanges", authHeaders());

        assertNotNull(response, "Gateway response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/stockExchanges when services are live, but got " + response.getStatusCode());
    }

    /**
     * Test that gateway correctly routes sector service requests
     */
    @Test
    void testGatewayRoutesToSectorService() {
        ResponseEntity<String> response = getWithRetry("/api/sectors", authHeaders());

        assertNotNull(response, "Gateway response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/sectors when services are live, but got " + response.getStatusCode());
    }

    /**
     * Test that gateway correctly routes stock price requests
     */
    @Test
    void testGatewayRoutesToStockPrices() {
        ResponseEntity<String> response = getWithRetry("/api/stock-prices", authHeaders());

        assertNotNull(response, "Gateway response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/stock-prices when services are live, but got " + response.getStatusCode());
    }

    /**
     * Test that gateway correctly routes IPO requests
     */
    @Test
    void testGatewayRoutesToIPOs() {
        ResponseEntity<String> response = getWithRetry("/api/ipos", authHeaders());

        assertNotNull(response, "Gateway response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/ipos when services are live, but got " + response.getStatusCode());
    }

    /**
     * Test that gateway correctly routes excel service requests
     */
    @Test
    void testGatewayRoutesToExcelService() {
        ResponseEntity<String> response = getWithRetry("/api/excel/health");

        assertNotNull(response, "Gateway response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/excel/health when services are live, but got " + response.getStatusCode());
    }

    /**
     * Test that gateway correctly routes user auth service requests
     */
    @Test
    void testGatewayRoutesToUserService() {
        ResponseEntity<String> response = getWithRetry("/api/auth/me", authHeaders());

        assertNotNull(response, "Gateway response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/auth/me when services are live, but got " + response.getStatusCode());
    }

    /**
     * Test that gateway returns 404 for unmapped routes
     */
    @Test
    void testGatewayReturns404ForUnmappedRoute() {
        ResponseEntity<String> response = getWithRetry("/api/unmapped-endpoint");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
            "Gateway should return 404 for unmapped routes");
    }
}
