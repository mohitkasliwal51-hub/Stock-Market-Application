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
                                // EXCHANGE-SERVICE routes (GET and POST for stockExchange)
                                .route("EXCHANGE-SERVICE", r -> r.path("/stockExchange/**")
                                                .and().method("GET", "POST")
                                                .uri("lb://EXCHANGE-SERVICE"))

                                // COMPANY-SERVICE routes (all controllers use /company path)
                                .route("COMPANY-SERVICE", r -> r.path("/company/**")
                                                .and().method("GET", "POST", "PUT", "DELETE")
                                                .uri("lb://COMPANY-SERVICE"))

                                // EXCEL-SERVICE routes (only POST for excel upload)
                                .route("EXCEL-SERVICE", r -> r.path("/excel/**")
                                                .and().method("POST")
                                                .uri("lb://EXCEL-SERVICE"))

                                // SECTOR-SERVICE routes (GET and POST for sectors)
                                .route("SECTOR-SERVICE", r -> r.path("/sectors/**")
                                                .and().method("GET", "POST")
                                                .uri("lb://SECTOR-SERVICE"))
                                .build();
        }
}
