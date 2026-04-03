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
                .route("STOCK-EXCHANGE-SERVICE", r -> r.path("/api/stockExchanges/**").uri("lb://EXCHANGE-SERVICE"))
                .route("COMPANY-SERVICE", r -> r.path("/api/companies/**").uri("lb://COMPANY-SERVICE"))
                .route("STOCKS-SERVICE", r -> r.path("/api/stocks/**").uri("lb://COMPANY-SERVICE"))
                .route("IPOS-SERVICE", r -> r.path("/api/ipos/**").uri("lb://COMPANY-SERVICE"))
                .route("STOCK-PRICES-SERVICE", r -> r.path("/api/stock-prices/**").uri("lb://COMPANY-SERVICE"))
                .route("EXCEL-SERVICE", r -> r.path("/api/excel/**").uri("lb://EXCEL-SERVICE"))
                .route("SECTOR-SERVICE", r -> r.path("/api/sectors/**").uri("lb://SECTOR-SERVICE"))
                .build();
    }
}
