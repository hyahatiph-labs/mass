package com.hiahatf.mass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping
@SpringBootApplication
public class MassApplication {

	public static void main(String[] args) {
		SpringApplication.run(MassApplication.class, args);
	}

	@GetMapping("/health")
	@ResponseStatus(HttpStatus.OK)
	public void ping() {
		// return http 200 status code
	}

}
