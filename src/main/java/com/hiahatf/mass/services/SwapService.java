package com.hiahatf.mass.services;

import java.io.IOException;

import javax.net.ssl.SSLException;

import com.hiahatf.mass.exception.MassException;
import com.hiahatf.mass.models.SwapRequest;
import com.hiahatf.mass.models.SwapResponse;
import com.hiahatf.mass.models.monero.XmrQuoteTable;
import com.hiahatf.mass.repo.QuoteRepository;
import com.hiahatf.mass.services.rpc.Lightning;
import com.hiahatf.mass.services.rpc.Monero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service("SwapService")
public class SwapService {
    
    private QuoteRepository quoteRepository;
    private Monero monero;
    private Lightning lightning;

    /**
     * Swap service dependency injection
     */
    @Autowired
    public SwapService(QuoteRepository quoteRepository,
        Lightning lightning, Monero monero) {
        this.quoteRepository = quoteRepository;
        this.lightning = lightning;
        this.monero = monero;
    }

    /**
     * Logic for processing the swap
     * 1. Verify that the lightning payment is in process
     * 2. Initiate the Monero Swap
     * 3. Remove the quote from db is success
     * TODO: min, max and xmr balance checks to verify we can do the swap
     * @param SwapRequest
     * @return SwapResponse
     */
    public Mono<SwapResponse> processMoneroSwap(SwapRequest request) {
        // fetch quote, if not fulfilled
        XmrQuoteTable quote = 
            quoteRepository.findById(request.getQuoteId()).get();
        if(!quote.getFulfilled()) {
            initiateMoneroSwap(quote);
        }
        return Mono.error(new MassException("Quote not found"));
    }

    /**
     * Helper method for processing the monero swap.
     * Does all the heavy lifting and error handling.
     * @param quote
     * @return Mono<SwapResponse>
     */
    private Mono<SwapResponse> initiateMoneroSwap(XmrQuoteTable quote) {
        // verify inflight payment
        try {
            return lightning.settleInvoice(quote.getPreimage()).flatMap(i -> {
                if(i.getStatusCode() == HttpStatus.OK) {
                    // send monero
                    // TODO: fallback for xmr transfer?
                    return monero.transfer(quote.getXmr_address(), quote.getAmount()).flatMap(r -> {
                        SwapResponse res = SwapResponse.builder()
                        .quoteId(quote.getQuote_id())
                        .txId(r.getResult().getTx_hash())
                        .build();
                        // remove quote from db
                        quoteRepository.deleteById(quote.getQuote_id());
                        return Mono.just(res);
                    });
                }
                return Mono.error(new MassException("Swap failed"));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

}
