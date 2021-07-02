package com.hiahatf.mass.services;

import com.hiahatf.mass.exception.MassException;
import com.hiahatf.mass.models.MoneroQuote;
import com.hiahatf.mass.models.MoneroRequest;
import com.hiahatf.mass.services.rate.RateService;
import com.hiahatf.mass.util.MassUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@Service("QuoteService")
public class QuoteService {

    private RateService rateService;
    private MassUtil massUtil;

    @Autowired
    public QuoteService(RateService rateService, MassUtil massUtil) {
        this.rateService = rateService;
        this.massUtil = massUtil;
    }

    // TODO: validate address from request
    private boolean isValidMoneroAddress(String address) throws MassException {
        throw new MassException("Invalid address");
    }

    // TODO: save quote to db with status

    // TODO: generate lightning invoice for the quote

    /**
     * Helper method for building the monero quote
     * and returning it to the client
     * @return MoneroQuote
     */
    public Mono<MoneroQuote> processMoneroRequest(MoneroRequest request) {
        try {
            isValidMoneroAddress(request.getAddress());
        } catch (MassException me) {
            throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, me.getMessage());
        }
         MoneroQuote quote = MoneroQuote.builder()
        .address(request.getAddress())
        .amount(request.getAmount())
        .invoice("lninvoice123" /* TODO: generate hold invoice */)
        // this is super ugly, TODO: fix it
        .rate(massUtil.splitMoneroRate(rateService.getMoneroRate().block()))
        .build();
        return Mono.just(quote);
    }

}
