package org.hiahatf.mass.services.bitcoin;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.services.rate.RateService;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.util.MassUtil;
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
    private RateService rateService;
    private MassUtil massUtil;
    private String sendAddress;
    private Long minPay;
    private Long maxPay;

    @Autowired
    public QuoteService(
        Lightning lightning, 
        @Value(Constants.SEND_ADDRESS) String sendAddress,
        @Value(Constants.MIN_PAY) Long minPay,
        @Value(Constants.MAX_PAY) Long maxPay,
        MassUtil massUtil,
        RateService rateService) {
        this.lightning = lightning;
        this.sendAddress = sendAddress;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.massUtil = massUtil;
        this.rateService = rateService;
    }
    

    public Mono<Quote> processBitcoinQuote(Request request) {
        return rateService.getMoneroRate().flatMap(r -> {
            Double parsedRate = massUtil.parseMoneroRate(r);
            return decodePayReq(request, parsedRate);
        });
        
    // TODO: Save Payment hash / request to db with refund address

    // TODO: Return quote
    }

    /**
     * Call Lightning API for decoding the payment request
     * @param request
     * @param rate
     * @return Mono<Quote>
     */
    private Mono<Quote> decodePayReq(Request request, Double rate) {
        String pr = request.getPaymentRequest();
        try {
            return lightning.decodePaymentRequest(pr).flatMap(p -> {
                // validate expiry is not set for a longer than limit
                if(Integer.valueOf(p.getExpiry()) > Constants.EXPIRY_LIMIT) {
                    return Mono.error(new MassException(Constants.EXPIRY_ERROR));
                }
                return massUtil.validateInboundLiquidity(Double.valueOf(p.getNum_satoshis())).flatMap(l -> {
                    if(l.booleanValue()) {
                        Quote quote = Quote.builder()
                            .paymentRequest(pr)
                            .quoteId(p.getPayment_hash())
                            .sendAddress(sendAddress)
                            .rate(rate)
                            .minSwapAmt(minPay)
                            .maxSwapAmt(maxPay)
                            .build();
                        return Mono.just(quote);
                    }
                    return Mono.error(new MassException(Constants.DECODE_ERROR));
                });
        });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }

    }
    
}
