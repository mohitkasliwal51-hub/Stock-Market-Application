package com.sankalp.marketservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Market Service API")
						.description("Market hours and constrained live price simulation APIs")
						.version("1.0.0")
						.contact(new Contact()
								.name("Stock Market Application Team")
								.email("support@stockmarket.com")
								.url("https://stockmarket.com"))
						.license(new License()
								.name("Apache 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0")))
				.servers(List.of(
						new Server()
								.url("http://localhost:8089")
								.description("Development Server"),
						new Server()
								.url("https://api.stockmarket.com")
								.description("Production Server")))
				.tags(List.of(
						new Tag()
								.name("Market")
								.description("Market status and live simulation operations")));
	}
}
