package com.hiahatf.mass.controllers;

import java.io.IOException;

import javax.net.ssl.SSLException;

import com.hiahatf.mass.services.rpc.Lightning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Health Check Controller
 */
@RequestMapping
@RestController
public class HealthController extends BaseController{
    
	private Lightning lightning;

	@Autowired
	public HealthController(Lightning lightning) {
		this.lightning = lightning;
	}

    @GetMapping("/health")
	@ResponseStatus(HttpStatus.OK)
	public Mono<String> ping() throws SSLException, IOException {
		// return http 200 status code
		return lightning.getInfo();
	}
    
}
