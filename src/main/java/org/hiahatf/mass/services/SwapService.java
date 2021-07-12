package org.hiahatf.mass.services;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.InvoiceState;
import org.hiahatf.mass.models.SwapRequest;
import org.hiahatf.mass.models.SwapResponse;
import org.hiahatf.mass.models.monero.MoneroTranserResponse;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.repo.QuoteRepository;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling all swap logic
 */
@Service("SwapService")
public class SwapService {
    
    private QuoteRepository quoteRepository;
    private Monero monero;
    private Lightning lightning;

    /**
     * Swap service dependency injection
     */
    @Autowired
    public SwapService(
        QuoteRepository quoteRepository, 
        Lightning lightning, 
        Monero monero) {
            this.quoteRepository = quoteRepository;
            this.lightning = lightning;
            this.monero = monero;
    }

    /**
     * Logic for processing the swap
     * 1. Verify that the lightning invoice is ACCEPTED
     * 2. Initiate the Monero Swap
     * 3. Remove the quote from db if success
     * @param SwapRequest
     * @return SwapResponse
     */
    public Mono<SwapResponse> processMoneroSwap(SwapRequest request) {
        // fetch quote, if not fulfilled
        XmrQuoteTable quote = quoteRepository.findById(request.getHash()).get();
        // verify inflight payment, state should be ACCEPTED
        try {
            return lightning.lookupInvoice(quote.getQuote_id()).flatMap(l -> {
                if(l.getState() == InvoiceState.ACCEPTED) {
                    return transferMonero(quote);
                }
                return Mono.error(new MassException("Payment not in flight"));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

    /**
     * Perform Monero transfer and settle or cancel the invoice
     * @param quote
     * @return
     */
    private Mono<SwapResponse> transferMonero(XmrQuoteTable quote) {
        return monero.transfer(quote.getXmr_address(), 
        quote.getAmount()).flatMap(r -> {
            // null check, since rpc since 200 on null result
            if(r.getResult() == null) {
                // monero transfer failed, cancel invoice
                return cancelMoneroSwap(quote);
            }
            return settleMoneroSwap(quote, r);
        });
    }

    /**
     * Helper method for cancelling the Monero swap.
     * @param quote
     * @return Mono
     */
    private Mono<SwapResponse> cancelMoneroSwap(XmrQuoteTable quote) {
        try {
            return lightning.handleInvoice(quote, false).flatMap(c -> {
                if(c.getStatusCode() == HttpStatus.OK) {
                    return Mono.error(new MassException("Unable to send XMR. Invoice cancelled"));
                }
                return Mono.error(new MassException("XMR transfer failure!"));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

    /**
     * Helper method for settling the Monero swap.
     * @param quote
     * @return Mono<SwapResponse>
     */
    private Mono<SwapResponse> settleMoneroSwap(XmrQuoteTable quote, 
    MoneroTranserResponse r) {
        try {
            return lightning.handleInvoice(quote, true).flatMap(c -> {
                if(c.getStatusCode() == HttpStatus.OK) {
                    // monero transfer succeeded, settle invoice
                    SwapResponse res = SwapResponse.builder()
                        .hash(quote.getQuote_id())
                        .txId(r.getResult().getTx_hash())
                        .build();
                    // remove quote from db
                    quoteRepository.deleteById(quote.getQuote_id());
                    return Mono.just(res);
                }
                return Mono.error(new MassException("Fatal, swap failure!"));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

}
