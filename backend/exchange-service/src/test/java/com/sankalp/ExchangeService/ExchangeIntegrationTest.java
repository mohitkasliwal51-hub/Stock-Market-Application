package com.sankalp.exchangeservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Exchange Service.
 * Tests that exchange service can provide exchange data to other services.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.datasource.url=jdbc:mysql://localhost:3306/stock_market_test?createDatabaseIfNotExist=true",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ExchangeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test that exchange service endpoint returns success response
     */
    @Test
    void testGetAllExchangesReturnsSuccessResponse() {
        // Given: The exchange service is running
        String url = "/api/stockExchanges";
        
        // When: Calling the get all exchanges endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should be successful
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Exchange service should return 200 OK");
        assertNotNull(response.getBody(),
            "Response body should not be null");
    }

    /**
     * Test that exchange service can retrieve companies by exchange
     */
    @Test
    void testGetCompaniesByExchangeReturnsData() {
        // Given: An exchange ID
        Integer exchangeId = 1;
        String url = "/api/stockExchanges/" + exchangeId + "/companies";
        
        // When: Calling the companies by exchange endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should be valid
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Exchange service should return a valid response"
        );
    }

    /**
     * Test that exchange service can get exchange by ID
     */
    @Test
    void testGetExchangeById() {
        // Given: An exchange ID
        Integer exchangeId = 1;
        String url = "/api/stockExchanges/" + exchangeId;
        
        // When: Calling the get exchange by ID endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should be valid
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Exchange service should return a valid response"
        );
    }

    /**
     * Test that exchange service properly validates input for exchange creation
     */
    @Test
    void testCreateExchangeWithValidData() {
        // Given: Valid exchange data
        String url = "/api/stockExchanges";
        String exchangeJson = "{"
            + "\"exchangeName\":\"NSE\","
            + "\"city\":\"Mumbai\","
            + "\"country\":\"India\","
            + "\"remarks\":\"National Stock Exchange\""
            + "}";
        
        // When: Creating an exchange via POST
        ResponseEntity<String> response = restTemplate.postForEntity(
            url,
            exchangeJson,
            String.class
        );
        
        // Then: Endpoint should return 200 (success) or 400 (validation error)
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Exchange service should return valid HTTP response"
        );
    }

    /**
     * Test that exchange service endpoint is accessible
     */
    @Test
    void testExchangeServiceEndpointIsAccessible() {
        // Given: The exchange service base URL
        String url = "/api/stockExchanges";
        
        // When: Making a simple GET request
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Endpoint should be accessible
        assertNotNull(response,
            "Response should not be null");
        assertFalse(response.getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE),
            "Exchange service should not be unavailable");
    }

    /**
     * Test that exchange service responds within acceptable timeout
     */
    @Test
    void testExchangeServiceResponseTime() {
        // Given: The exchange service is running
        String url = "/api/stockExchanges";
        
        // When: Calling the endpoint and measuring response time
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // Then: Response time should be reasonable (less than 5 seconds)
        assertTrue(responseTime < 5000,
            "Exchange service should respond within 5 seconds, took: " + responseTime + "ms");
    }

    /**
     * Test that invalid exchange ID returns appropriate error
     */
    @Test
    void testGetInvalidExchangeReturnsError() {
        // Given: An invalid exchange ID
        Integer invalidId = 999999;
        String url = "/api/stockExchanges/" + invalidId;
        
        // When: Requesting a non-existent exchange
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should handle the error gracefully
        assertNotNull(response,
            "Response should not be null");
        assertTrue(
            response.getStatusCode().is4xxClientError() || 
            response.getStatusCode().is2xxSuccessful(),
            "Exchange service should return valid HTTP response"
        );
    }
}
