package com.ppm.secureapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringSecurityJwtJpaV2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityJwtJpaV2Application.class, args);
	}

}
