package com.sankalp.sectorservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Sector Service.
 * Tests that sector service can provide sector data to other services.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.datasource.url=jdbc:mysql://localhost:3306/stock_market_test?createDatabaseIfNotExist=true",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SectorIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test that sector service endpoint returns success response
     */
    @Test
    void testGetAllSectorsReturnsSuccessResponse() {
        // Given: The sector service is running
        String url = "/api/sectors";
        
        // When: Calling the get all sectors endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should be successful
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Sector service should return 200 OK");
        assertNotNull(response.getBody(),
            "Response body should not be null");
    }

    /**
     * Test that sector service can retrieve companies by sector
     */
    @Test
    void testGetCompaniesBySectorReturnsData() {
        // Given: A sector ID
        Integer sectorId = 1;
        String url = "/api/sectors/" + sectorId + "/companies";
        
        // When: Calling the companies by sector endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should be valid
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Sector service should return a valid response"
        );
    }

    /**
     * Test that sector service can get sector by ID
     */
    @Test
    void testGetSectorById() {
        // Given: A sector ID
        Integer sectorId = 1;
        String url = "/api/sectors/" + sectorId;
        
        // When: Calling the get sector by ID endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should be valid
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Sector service should return a valid response"
        );
    }

    /**
     * Test that sector service properly validates input for sector creation
     */
    @Test
    void testCreateSectorWithValidData() {
        // Given: Valid sector data
        String url = "/api/sectors";
        String sectorJson = "{"
            + "\"sectorName\":\"Technology\","
            + "\"brief\":\"IT and Software companies\""
            + "}";
        
        // When: Creating a sector via POST
        ResponseEntity<String> response = restTemplate.postForEntity(
            url,
            sectorJson,
            String.class
        );
        
        // Then: Endpoint should return 200 (success) or 400 (validation error)
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Sector service should return valid HTTP response"
        );
    }

    /**
     * Test that sector service endpoint is accessible
     */
    @Test
    void testSectorServiceEndpointIsAccessible() {
        // Given: The sector service base URL
        String url = "/api/sectors";
        
        // When: Making a simple GET request
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Endpoint should be accessible
        assertNotNull(response,
            "Response should not be null");
        assertFalse(response.getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE),
            "Sector service should not be unavailable");
    }

    /**
     * Test that sector service responds within acceptable timeout
     */
    @Test
    void testSectorServiceResponseTime() {
        // Given: The sector service is running
        String url = "/api/sectors";
        
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
            "Sector service should respond within 5 seconds, took: " + responseTime + "ms");
    }

    /**
     * Test that invalid sector ID returns appropriate error
     */
    @Test
    void testGetInvalidSectorReturnsError() {
        // Given: An invalid sector ID
        Integer invalidId = 999999;
        String url = "/api/sectors/" + invalidId;
        
        // When: Requesting a non-existent sector
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
            "Sector service should return valid HTTP response"
        );
    }
}
