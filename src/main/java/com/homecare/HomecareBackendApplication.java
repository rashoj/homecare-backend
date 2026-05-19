package com.homecare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HomecareBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomecareBackendApplication.class, args);
	}

}
