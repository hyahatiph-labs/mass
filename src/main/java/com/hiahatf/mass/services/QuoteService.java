package com.hiahatf.mass.services;

import com.hiahatf.mass.models.MoneroQuote;
import com.hiahatf.mass.models.MoneroRequest;
import com.hiahatf.mass.services.rate.RateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service("QuoteService")
public class QuoteService {

    private RateService rateService;

    @Autowired
    public QuoteService(RateService rateService) {
        this.rateService = rateService;
    }

    // TODO: validate address from request

    // TODO: save quote to db with status

    // TODO: generate lightning invoice for the quote

    // TODO: return quote with necessary data

    /**
     * Helper method for building the monero quote
     * and returning it to the client
     * @return MoneroQuote
     */
    public Mono<MoneroQuote> processMoneroRequest(MoneroRequest request) {
         MoneroQuote quote = MoneroQuote.builder()
        .address(request.getAddress())
        .amount(request.getAmount())
        .invoice("lninvoice123" /* TODO: generate hold invoice */)
        // this is super ugly, TODO: fix it
        .rate(rateService.getMoneroRate().block().split(":")[1].split("}")[0])
        .build();
        return Mono.just(quote);
    }

}
