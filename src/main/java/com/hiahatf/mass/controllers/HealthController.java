package com.hiahatf.mass.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check Controller
 */
@RequestMapping
@RestController
public class HealthController {
    
    @GetMapping("/health")
	@ResponseStatus(HttpStatus.OK)
	public void ping() {
		// return http 200 status code
	}
    
}
