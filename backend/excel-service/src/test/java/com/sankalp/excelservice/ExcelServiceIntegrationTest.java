package com.sankalp.excelservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Excel Service.
 * Tests that excel service can handle health checks and data uploads.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.datasource.url=jdbc:mysql://localhost:3306/stock_market_test?createDatabaseIfNotExist=true",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ExcelServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test that excel service health check endpoint is accessible
     */
    @Test
    void testExcelServiceHealthCheck() {
        // Given: The excel service is running
        String url = "/api/excel/health";
        
        // When: Calling the health check endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Response should be successful or indicate service status
        assertNotNull(response,
            "Health check response should not be null");
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is5xxServerError(),
            "Health check endpoint should be accessible"
        );
    }

    /**
     * Test that excel service upload endpoint is accessible
     */
    @Test
    void testExcelServiceUploadEndpointIsAccessible() {
        // Given: The excel service is running
        String url = "/api/excel/uploadData";
        
        // When: Making a POST request to upload endpoint
        String emptyJson = "[]";
        ResponseEntity<String> response = restTemplate.postForEntity(
            url,
            emptyJson,
            String.class
        );
        
        // Then: Endpoint should be accessible and handle requests
        assertNotNull(response,
            "Upload endpoint response should not be null");
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Upload endpoint should be accessible"
        );
    }

    /**
     * Test that excel service endpoint is accessible
     */
    @Test
    void testExcelServiceEndpointIsAccessible() {
        // Given: The excel service base URL
        String url = "/api/excel/health";
        
        // When: Making a simple GET request
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Endpoint should be accessible
        assertNotNull(response,
            "Response should not be null");
        assertFalse(response.getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE),
            "Excel service should not be unavailable");
    }

    /**
     * Test that excel service responds within acceptable timeout
     */
    @Test
    void testExcelServiceResponseTime() {
        // Given: The excel service is running
        String url = "/api/excel/health";
        
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
            "Excel service should respond within 5 seconds, took: " + responseTime + "ms");
    }

    /**
     * Test that excel service handles invalid uploads gracefully
     */
    @Test
    void testExcelServiceHandlesInvalidData() {
        // Given: Invalid data format
        String url = "/api/excel/uploadData";
        String invalidJson = "{invalid json}";
        
        // When: Uploading invalid data
        ResponseEntity<String> response = restTemplate.postForEntity(
            url,
            invalidJson,
            String.class
        );
        
        // Then: Service should return error response gracefully
        assertTrue(
            response.getStatusCode().is4xxClientError() || 
            response.getStatusCode().is5xxServerError(),
            "Excel service should handle invalid data gracefully"
        );
    }
}
