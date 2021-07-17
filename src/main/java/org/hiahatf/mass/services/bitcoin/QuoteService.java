package org.hiahatf.mass.services.bitcoin;

import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling Monero quote logic
 */
@Service
public class QuoteService {
    
    // TODO: Accept and decode payment request 

    // TODO: Save Payment hash to db with refund address

    // TODO: Query a route to verify possible payment
    
    // TODO: Quotes Return payment route / hints, send address and amount


    public Mono<Quote> processBitcoinQuote(Request request) {
        Quote quote = Quote.builder().build();
        return Mono.just(quote);
    }
    
}
