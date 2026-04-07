package com.sankalp.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // STOCK-EXCHANGE-SERVICE routes (all CRUD operations)
                .route("STOCK-EXCHANGE-SERVICE", r -> r.path("/api/stockExchanges/**")
                        .and().method("GET", "POST", "PUT", "DELETE")
                        .uri("lb://EXCHANGE-SERVICE"))
                        
                // COMPANY-SERVICE routes (all CRUD operations)
                .route("COMPANY-SERVICE", r -> r.path("/api/companies/**")
                        .and().method("GET", "POST", "PUT", "DELETE")
                        .uri("lb://COMPANY-SERVICE"))
                
                // STOCKS routes (company-service)
                .route("STOCKS-SERVICE", r -> r.path("/api/stocks/**")
                        .and().method("GET", "POST", "PUT", "DELETE")
                        .uri("lb://COMPANY-SERVICE"))
                
                // IPOS routes (company-service)
                .route("IPOS-SERVICE", r -> r.path("/api/ipos/**")
                        .and().method("GET", "POST", "PUT", "DELETE")
                        .uri("lb://COMPANY-SERVICE"))
                
                // STOCK-PRICES routes (company-service)
                .route("STOCK-PRICES-SERVICE", r -> r.path("/api/stock-prices/**")
                        .and().method("GET", "POST")
                        .uri("lb://COMPANY-SERVICE"))
                        
                // EXCEL-SERVICE routes (health check and data upload)
                .route("EXCEL-SERVICE", r -> r.path("/api/excel/**")
                        .and().method("GET", "POST")
                        .uri("lb://EXCEL-SERVICE"))
                        
                // SECTOR-SERVICE routes (all CRUD operations)
                .route("SECTOR-SERVICE", r -> r.path("/api/sectors/**")
                        .and().method("GET", "POST", "PUT", "DELETE")
                        .uri("lb://SECTOR-SERVICE"))

                // USER-SERVICE auth routes
                .route("USER-SERVICE", r -> r.path("/api/auth/**")
                        .and().method("GET", "POST")
                        .uri("lb://USER-SERVICE"))

                // MARKET-SERVICE routes
                .route("MARKET-SERVICE", r -> r.path("/api/market/**")
                        .and().method("GET", "POST")
                        .uri("lb://MARKET-SERVICE"))
                .build();
    }
}
