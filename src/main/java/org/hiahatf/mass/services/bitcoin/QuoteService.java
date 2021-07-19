package org.hiahatf.mass.services.bitcoin;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.services.rpc.Lightning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling Bitcoin quote logic
 */
@Service(Constants.BTC_QUOTE_SERVICE)
public class QuoteService {

    private Lightning lightning;
    private String sendAddress;
    private Long minPay;
    private Long maxPay;

    @Autowired
    public QuoteService(
        Lightning lightning, 
        @Value(Constants.SEND_ADDRESS) String sendAddress,
        @Value(Constants.MIN_PAY) Long minPay,
        @Value(Constants.MAX_PAY) Long maxPay) {
        this.lightning = lightning;
        this.sendAddress = sendAddress;
        this.minPay = minPay;
        this.maxPay = maxPay;
    }
    

    public Mono<Quote> processBitcoinQuote(Request request) {
        String pr = request.getPaymentRequest();
        try {
            return lightning.decodePaymentRequest(pr).flatMap(p -> {
                if(Integer.valueOf(p.getExpiry()) > Constants.EXPIRY_LIMIT) {
                    return Mono.error(new MassException(Constants.EXPIRY_ERROR));
                }
                if(p.getNum_satoshis() != Constants.INVOICE_LIMIT) {
                    return Mono.error(new MassException(Constants.INVOICE_ERROR));
                }
                    Quote quote = Quote.builder()
                        .paymentRequest(pr)
                        .quoteId(p.getPayment_hash())
                        .sendAddress(sendAddress)
                        .rate(0.00777)
                        .minSwapAmt(minPay)
                        .maxSwapAmt(maxPay)
                        .build();
                return Mono.just(quote);
        });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    
    // TODO; Send rate too!

    // TODO: Accept and decode payment request, validate expiry / amounts

    // TODO: Get route probability to verify possible payment
        
    // TODO: Save Payment hash / request to db with refund address

    // TODO: Return quote with, route possiblility, send address and amount
    }
    
}
