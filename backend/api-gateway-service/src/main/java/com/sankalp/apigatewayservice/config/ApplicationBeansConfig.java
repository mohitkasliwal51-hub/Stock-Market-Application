package com.sankalp.apigatewayservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Central configuration class for application infrastructure beans.
 * This class consolidates all utility and infrastructure-level bean definitions.
 * 
 * Beans managed here:
 * - RestTemplate: For inter-service HTTP communication
 * - ObjectMapper: For JSON serialization/deserialization
 */
@Configuration
public class ApplicationBeansConfig {

    /**
     * Configures RestTemplate bean for making HTTP requests to other services.
     * Includes connection and read timeouts to prevent hanging requests.
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        return new RestTemplate(requestFactory);
    }

    /**
     * Provides ObjectMapper bean for JSON serialization and deserialization.
     * Configured for common use cases in REST APIs.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Additional configurations can be added here if needed
        // e.g., mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }
}
