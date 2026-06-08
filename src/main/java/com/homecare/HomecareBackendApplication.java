package com.homecare;

import com.homecare.entity.Role;
import com.homecare.entity.User;
import com.homecare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class HomecareBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomecareBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner seedAdmin(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder
	) {
		return args -> {

			if (userRepository.findByEmail("admin@homecare.com").isEmpty()) {

				User admin = new User();

				admin.setFullName("Admin");
				admin.setEmail("admin@homecare.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Role.ADMIN);

				userRepository.save(admin);

				System.out.println("Admin user seeded.");
			}

			String productionHash =
					"$2a$10$H6ZmggGki5cZUq6IYkf6yeFMXRBILvynlw1Vy/SPqwznBCdM9uxka";

			System.out.println("=================================");
			System.out.println("PASSWORD TEST = " +
					passwordEncoder.matches("Password123!", productionHash)
			);

			String freshHash = passwordEncoder.encode("Password123!");
			System.out.println("FRESH HASH = " + freshHash);
			System.out.println("FRESH HASH TEST = " +
					passwordEncoder.matches("Password123!", freshHash)
			);
			System.out.println("=================================");
		};
	}
}