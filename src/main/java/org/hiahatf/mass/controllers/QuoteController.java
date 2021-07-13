package org.hiahatf.mass.controllers;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.hiahatf.mass.models.monero.MoneroRequest;
import org.hiahatf.mass.services.QuoteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Provides quotes that have information
 * pertaining to fees, amount, etc.
 */
@RestController
@RequestMapping
public class QuoteController extends BaseController {
    
    private QuoteService quoteService;

    /**
     * Quote Controller constructor dependency injection
     * @param quoteService
     */
    @Autowired
    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /**
     * This endpoint reaches out to an external
     * API to get the exchange rate and return
     * a quote when a request for Monero is received.
     * The quote contains a lightning network invoice
     * with additional details.
     * @return MoneroQuote
     */
    @GetMapping(Constants.XMR_QUOTE_PATH)
    public Mono<MoneroQuote> fetchMoneroQuote(@RequestBody MoneroRequest request) {
        return quoteService.processMoneroQuote(request);
    }

}
