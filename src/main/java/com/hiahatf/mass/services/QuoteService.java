package com.hiahatf.mass.services;

import com.hiahatf.mass.exception.MassException;
import com.hiahatf.mass.models.MoneroQuote;
import com.hiahatf.mass.models.MoneroRequest;
import com.hiahatf.mass.services.rate.RateService;
import com.hiahatf.mass.services.rpc.Monero;
import com.hiahatf.mass.util.MassUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service("QuoteService")
public class QuoteService {

    private static final String INVALID_ADDRESS = "Invalid address";
    private RateService rateService;
    private Monero moneroRpc;
    private MassUtil massUtil;

    @Autowired
    public QuoteService(RateService rateService, MassUtil massUtil, 
        Monero moneroRpc) {
            this.rateService = rateService;
            this.massUtil = massUtil;
            this.moneroRpc = moneroRpc;
    }

    /**
     * Method for building the monero quote
     * and returning it to the client
     * @return Mono<MoneroQuote>
     */
    public Mono<MoneroQuote> processMoneroQuote(MoneroRequest request) {
        return rateService.getMoneroRate().flatMap(r -> {
            // validate the address
            return validateMoneroAddress(request.getAddress()).flatMap(v -> {
                Double rate = massUtil.splitMoneroRate(r);
                MoneroQuote quote = MoneroQuote.builder()
                    .address(request.getAddress())
                    .isValidAddress(v)
                    .amount(request.getAmount())
                    .invoice("lninvoice123" /* TODO: generate hold invoice */)
                    .rate(rate)
                    .build();
                return Mono.just(quote);
            });   
        });
        // TODO: save quote to db with status
        // TODO: generate lightning invoice for the quote
    }

    /**
     * Validate the Monero address that will receive the swap
     * @param address
     * @return Mono<Boolean> - true if valid
     */
    private Mono<Boolean> validateMoneroAddress(String address) {
        return moneroRpc.validateAddress(address).flatMap(r -> {
                if(!r.getResult().isValid()) {
                    return Mono.error(new MassException(INVALID_ADDRESS));
                }
                return Mono.just(true);
            });
    }

}
