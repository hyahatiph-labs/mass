package org.hiahatf.mass.controllers;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.lightning.Info;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.models.Constants;

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
public class HealthController extends BaseController {
    
	private Lightning lightning;

	/**
	 * HealthController dependency injection
	 * @param lightning
	 */
	@Autowired
	public HealthController(Lightning lightning) {
		this.lightning = lightning;
	}

	/**
	 * Ping MASS and return information about the underlying
	 * LND node. 
	 * @return Mono<Info>
	 * @throws SSLException
	 * @throws IOException
	 */
    @GetMapping(Constants.HEALTH_PATH)
	@ResponseStatus(HttpStatus.OK)
	public Mono<Info> ping() throws SSLException, IOException {
		return lightning.getInfo();
	}
    
}
