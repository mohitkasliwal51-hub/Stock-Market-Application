package com.sankalp.walletservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	CommandLineRunner alignWalletTransactionEnum(JdbcTemplate jdbcTemplate) {
		return args -> jdbcTemplate.execute(
				"ALTER TABLE wallet_transaction MODIFY COLUMN transaction_type " +
				"ENUM('DEPOSIT','WITHDRAWAL','ORDER_RESERVE','ORDER_RELEASE','ORDER_DEBIT','ORDER_CREDIT') NOT NULL");
	}
}
