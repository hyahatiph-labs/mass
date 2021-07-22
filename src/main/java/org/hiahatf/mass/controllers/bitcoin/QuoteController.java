package org.hiahatf.mass.controllers.bitcoin;

import org.hiahatf.mass.controllers.BaseController;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.services.bitcoin.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Provides Bitcoin quotes that have information
 * pertaining to fees, amount, etc.
 */
@RequestMapping
@RestController(Constants.BTC_QUOTE_CONTROLLER)
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
     * a quote when a request for Bitcoin is received.
     * The quote contains a lightning network invoice
     * with additional details.
     * @return BitcoinQuote
     */
    @GetMapping(Constants.BTC_QUOTE_PATH)
    public Mono<Quote> fetchBitcoinQuote(@RequestBody Request request) {
        return quoteService.processBitcoinQuote(request);
    }
    
}
