package com.sankalp.apigatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080").description("Dev"))
                .addServersItem(new Server().url("https://api.stockmarket.com").description("Prod"))
                .info(new Info()
                        .title("Stock Market API Gateway")
                        .version("1.0.0")
                        .description("Central API Gateway for Stock Market Microservices")
                        .contact(new Contact()
                                .name("Stock Market Team")
                                .email("support@stockmarket.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
