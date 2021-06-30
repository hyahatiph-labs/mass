package com.hiahatf.mass.controllers;

import com.hiahatf.mass.models.MoneroQuote;
import com.hiahatf.mass.services.QuoteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides quotes that have information
 * pertaining to fees, amount, etc.
 */
@RestController
@RequestMapping
public class QuoteController {
    
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
    @GetMapping("/quote/xmr")
    public MoneroQuote fetchMoneroQuote() {
        

        return quoteService.buildMoneroQuote();
    }
}
