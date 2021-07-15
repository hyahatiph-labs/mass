package org.hiahatf.mass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the MASS application
 */
@SpringBootApplication
@EnableScheduling
public class MassApplication {

	public static void main(String[] args) {
		SpringApplication.run(MassApplication.class, args);
	}

}
