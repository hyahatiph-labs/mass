package org.hiahatf.mass.controllers;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.services.monero.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private ClientService service;

	/**
	 * ClientController dependency injection
	 */
	@Autowired
	public ClientController(ClientService service) {
        this.service = service;
	}

	/**
	 * Accept Client's Monero Address and amount requested
     * to quote generation. 
	 * @return Mono<MoneroQuote>
	 * @throws SSLException
	 * @throws IOException
	 */
    @GetMapping(Constants.CLIENT_XMR_QUOTE_PATH)
	@ResponseStatus(HttpStatus.OK)
	public Mono<Quote> generateMoneroQuote(@RequestBody Request request) {
		return service.relayQuote(request);
	}

}
