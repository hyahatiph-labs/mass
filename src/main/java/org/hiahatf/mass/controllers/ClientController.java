package org.hiahatf.mass.controllers;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Entry for client APIs
 */
@RequestMapping
@RestController
public class ClientController {

	/**
	 * HealthController dependency injection
	 * @param lightning
	 */
	@Autowired
	public ClientController() {
        // wip
	}

	/**
	 * Accept Client's Monero Address and amount requested
     * to quote generation. 
	 * @return Mono<Info>
	 * @throws SSLException
	 * @throws IOException
	 */
    @GetMapping(Constants.CLIENT_XMR_QUOTE_PATH)
	@ResponseStatus(HttpStatus.OK)
	public Mono<MoneroQuote> generateMoneroQuote() throws SSLException, IOException {
		return null;
	}
}
