package com.sankalp.userservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sankalp.userservice.entity.UserAccount;
import com.sankalp.userservice.entity.UserRole;
import com.sankalp.userservice.repository.UserAccountRepository;

@Configuration
public class UserBootstrapConfig {

	@Bean
	public CommandLineRunner seedUsers(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!repository.existsByUsername("admin")) {
				UserAccount admin = new UserAccount();
				admin.setUsername("admin");
				admin.setEmail("admin@stockapp.local");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(UserRole.ADMIN);
				repository.save(admin);
			}

			if (!repository.existsByUsername("user")) {
				UserAccount user = new UserAccount();
				user.setUsername("user");
				user.setEmail("user@stockapp.local");
				user.setPassword(passwordEncoder.encode("user123"));
				user.setRole(UserRole.INVESTOR);
				repository.save(user);
			}
		};
	}
}
