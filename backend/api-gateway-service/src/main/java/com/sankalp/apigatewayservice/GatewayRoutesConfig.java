package com.sankalp.apigatewayservice;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("EXCHANGE-SERVICE", r -> r.path("/stockExchange/**").uri("lb://EXCHANGE-SERVICE"))
                .route("COMPANY-SERVICE", r -> r.path("/company/**").uri("lb://COMPANY-SERVICE"))
                .route("EXCEL-SERVICE", r -> r.path("/excel/**").uri("lb://EXCEL-SERVICE"))
                .route("SECTOR-SERVICE", r -> r.path("/sectors/**").uri("lb://SECTOR-SERVICE"))
                .build();
    }
}
