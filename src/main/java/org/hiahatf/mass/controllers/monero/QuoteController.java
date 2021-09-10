package org.hiahatf.mass.controllers.monero;

import org.hiahatf.mass.controllers.BaseController;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.services.monero.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Provides Monero quotes that have information
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
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(Constants.XMR_QUOTE_PATH)
    public Mono<Quote> fetchMoneroQuote(@RequestBody Request request) {
        return quoteService.processMoneroQuote(request);
    }

}
