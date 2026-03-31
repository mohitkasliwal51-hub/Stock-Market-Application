package com.sankalp.companyservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class CompanyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompanyServiceApplication.class, args);
	}
	
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Company Microservice API")
						.version("1.0")
						.description("API Documentation for Company Microservice")
						.contact(new Contact()
								.name("Sankalp Jain")
								.url("https://sankalpjain99.github.io/")
								.email("sankalpjain99@gmail.com"))
						.license(new License()
								.name("API License")
								.url("https://github.com/sankalpjain99/Stock-Market-Application")));
	}

}
