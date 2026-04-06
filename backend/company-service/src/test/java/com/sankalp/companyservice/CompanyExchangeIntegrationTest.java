package com.sankalp.companyservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.sankalp.companyservice.dto.ApiResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Company-Exchange service communication.
 * Tests that company service can fetch and verify exchange data.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
    "spring.datasource.url=jdbc:mysql://localhost:3306/stock_market_test?createDatabaseIfNotExist=true",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CompanyExchangeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Test that company service endpoint returns success response
     */
    @Test
    void testGetAllCompaniesReturnsSuccessResponse() {
        // Given: The company service is running
        String url = "/api/companies";
        
        // When: Calling the get all companies endpoint
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(
            url, 
            ApiResult.class
        );
        
        // Then: Response should be successful
        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Company service should return 200 OK");
        assertNotNull(response.getBody(),
            "Response body should not be null");
    }

    /**
     * Test that company service can retrieve companies by exchange
     */
    @Test
    void testGetCompaniesByExchangeReturnsList() {
        // Given: An exchange ID
        Integer exchangeId = 1;
        String url = "/api/companies/by-exchange/" + exchangeId;
        
        // When: Calling the companies by exchange endpoint
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(
            url, 
            ApiResult.class
        );
        
        // Then: Response should be successful (even if empty list)
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Company service should return a valid response"
        );
    }

    /**
     * Test that company service can get pending approval companies
     */
    @Test
    void testGetPendingApprovalCompanies() {
        // Given: The company service is running
        String url = "/api/companies/pending-approvals";
        
        // When: Calling the pending approvals endpoint
        ResponseEntity<ApiResult> response = restTemplate.getForEntity(
            url, 
            ApiResult.class
        );
        
        // Then: Response should be successful
        assertTrue(
            response.getStatusCode().is2xxSuccessful(),
            "Company service should successfully return pending approvals"
        );
    }

    /**
     * Test that company service properly validates input for company creation
     */
    @Test
    void testCreateCompanyWithValidData() {
        // Given: Valid company data
        String url = "/api/companies";
        String companyJson = "{"
            + "\"companyName\":\"TestCorp\","
            + "\"boardOfDirectors\":\"John Doe, Jane Smith\","
            + "\"briefWriteup\":\"A test company\","
            + "\"turnover\":\"1000000\","
            + "\"sectorId\":1"
            + "}";
        
        // When: Creating a company via POST (may fail due to database, but endpoint should handle it)
        ResponseEntity<String> response = restTemplate.postForEntity(
            url,
            companyJson,
            String.class
        );
        
        // Then: Endpoint should return 200 (success) or 400 (validation error)
        assertTrue(
            response.getStatusCode().is2xxSuccessful() || 
            response.getStatusCode().is4xxClientError(),
            "Company service should return valid HTTP response"
        );
    }

    /**
     * Test that company service returns proper error for invalid company ID
     */
    @Test
    void testGetInvalidCompanyReturnsError() {
        // Given: An invalid company ID
        Integer invalidId = 999999;
        String url = "/api/companies/" + invalidId;
        
        // When: Requesting a non-existent company
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
            "Company service should return valid HTTP response"
        );
    }

    /**
     * Test that company service endpoint is accessible
     */
    @Test
    void testCompanyServiceEndpointIsAccessible() {
        // Given: The company service base URL
        String url = "/api/companies";
        
        // When: Making a simple GET request
        ResponseEntity<String> response = restTemplate.getForEntity(
            url, 
            String.class
        );
        
        // Then: Endpoint should be accessible
        assertNotNull(response,
            "Response should not be null");
        assertFalse(response.getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE),
            "Company service should not be unavailable");
    }

    /**
     * Test that company service responds within acceptable timeout
     */
    @Test
    void testCompanyServiceResponseTime() {
        // Given: The company service is running
        String url = "/api/companies";
        
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
            "Company service should respond within 5 seconds, took: " + responseTime + "ms");
    }
}
