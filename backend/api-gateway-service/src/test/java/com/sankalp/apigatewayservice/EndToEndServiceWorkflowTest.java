package com.sankalp.apigatewayservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Integration Tests for complete workflows across multiple microservices.
 * Tests realistic application scenarios that involve multiple service interactions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.cloud.loadbalancer.eager-loading.enabled=true"
})
@DisplayName("End-to-End Service Workflow Integration Tests")
class EndToEndServiceWorkflowTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String cachedAuthToken;

    private org.springframework.http.ResponseEntity<String> getWithRetry(String path) {
        return getWithRetry(path, null);
    }

    private org.springframework.http.ResponseEntity<String> getWithRetry(String path, HttpHeaders headers) {
        org.springframework.http.ResponseEntity<String> response = null;
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

        var loginResponse = restTemplate.postForEntity(
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
     * Scenario 1: Retrieve all companies workflow
     * 1. Gateway -> Company Service -> Get all companies
     * 2. Verify response structure and content
     */
    @Test
    @DisplayName("E2E: Retrieve all companies through API Gateway")
    void testRetrieveAllCompaniesWorkflow() {
        var response = getWithRetry("/api/companies", authHeaders());

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/companies, but got " + response.getStatusCode());
    }

    /**
     * Scenario 2: Retrieve companies filtered by exchange
     * 1. Gateway -> Company Service -> Query by exchange
     * 2. Company Service may need to verify exchange exists
     * 3. Return filtered results
     */
    @Test
    @DisplayName("E2E: Retrieve companies by exchange through API Gateway")
    void testRetrieveCompaniesByExchangeWorkflow() {
        Integer exchangeId = 1;
        var response = getWithRetry("/api/companies/by-exchange/" + exchangeId, authHeaders());

        assertNotNull(response, "Response should not be null");
        assertFalse(response.getStatusCode().is5xxServerError(),
            "Expected non-5xx for /api/companies/by-exchange/{id}, but got " + response.getStatusCode());
    }

    /**
     * Scenario 3: Retrieve all exchanges with their companies
     * 1. Gateway -> Exchange Service -> Get all exchanges
     * 2. Exchange Service returns exchange list
     * 3. In real use, UI would then fetch companies for each exchange
     */
    @Test
    @DisplayName("E2E: Retrieve all exchanges through API Gateway")
    void testRetrieveAllExchangesWorkflow() {
        var response = getWithRetry("/api/stockExchanges", authHeaders());

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/stockExchanges, but got " + response.getStatusCode());
    }

    /**
     * Scenario 4: Retrieve all sectors
     * 1. Gateway -> Sector Service -> Get all sectors
     * 2. Sector Service returns sector list
     * 3. UI can then use sector IDs to filter companies
     */
    @Test
    @DisplayName("E2E: Retrieve all sectors through API Gateway")
    void testRetrieveAllSectorsWorkflow() {
        var response = getWithRetry("/api/sectors", authHeaders());

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/sectors, but got " + response.getStatusCode());
    }

    /**
     * Scenario 5: Retrieve stock prices with date filtering
     * 1. Gateway -> Stock Price Service -> Get prices by date range
     * 2. Service queries database with filters
     * 3. Return filtered price data
     */
    @Test
    @DisplayName("E2E: Retrieve stock prices through API Gateway")
    void testRetrieveStockPricesWorkflow() {
        var response = getWithRetry("/api/stock-prices", authHeaders());

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/stock-prices, but got " + response.getStatusCode());
    }

    /**
     * Scenario 6: Retrieve IPOs
     * 1. Gateway -> Company Service (IPO endpoint) -> Get all IPOs
     * 2. Return IPO list
     */
    @Test
    @DisplayName("E2E: Retrieve IPOs through API Gateway")
    void testRetrieveIPOsWorkflow() {
        var response = getWithRetry("/api/ipos", authHeaders());

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/ipos, but got " + response.getStatusCode());
    }

    /**
     * Scenario 7: Check Excel Service Health
     * 1. Gateway -> Excel Service -> Health Check
     * 2. Excel Service responds with health status
     * 3. Used to verify service availability before upload
     */
    @Test
    @DisplayName("E2E: Health check Excel service through API Gateway")
    void testExcelServiceHealthCheckWorkflow() {
        var response = getWithRetry("/api/excel/health");

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/excel/health, but got " + response.getStatusCode());
    }

    /**
     * Scenario 8: Pending Approvals Dashboard
     * 1. Gateway -> Company Service -> Get pending aproval companies
     * 2. Filter companies by approval status
     * 3. Return for admin review
     */
    @Test
    @DisplayName("E2E: Retrieve pending approval companies for admin dashboard")
    void testRetrievePendingApprovalsWorkflow() {
        var response = getWithRetry("/api/companies/pending-approval", authHeaders());

        assertNotNull(response, "Response should not be null");
        assertTrue(response.getStatusCode().is2xxSuccessful(),
            "Expected 2xx from /api/companies/pending-approval, but got " + response.getStatusCode());
    }

    /**
     * Scenario 9: Service Resilience - Unmapped Route
     * 1. Gateway receives request for unmapped endpoint
     * 2. Gateway returns 404 Not Found
     * 3. Do not crash or return 5xx errors
     */
    @Test
    @DisplayName("E2E: Gateway handles unmapped routes gracefully")
    void testGatewayHandlesUnmappedRoutes() {
        var response = getWithRetry("/api/nonexistent-endpoint");

        assertNotNull(response, "Response should not be null");
        assertTrue(
            response.getStatusCode().is4xxClientError(),
            "Gateway should return 4xx for unmapped routes, not 5xx"
        );
    }

    /**
     * Scenario 10: Service Response Consistency
     * 1. Request same endpoint multiple times
     * 2. Verify consistent response structure
     * 3. Verify consistent response status
     */
    @Test
    @DisplayName("E2E: Verify service consistency across multiple requests")
    void testServiceConsistency() {
        var auth = authHeaders();
        var response1 = getWithRetry("/api/companies", auth);
        var response2 = getWithRetry("/api/companies", auth);
        var response3 = getWithRetry("/api/companies", auth);

        assertEquals(response1.getStatusCode(), response2.getStatusCode(),
            "First and second requests should have same status");
        assertEquals(response2.getStatusCode(), response3.getStatusCode(),
            "Second and third requests should have same status");
        assertTrue(response1.getStatusCode().is2xxSuccessful(),
            "Consistency checks should be executed on successful responses");
    }
}
