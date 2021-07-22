package org.hiahatf.mass.services.bitcoin;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.bitcoin.BtcQuoteTable;
import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.models.lightning.PaymentRequest;
import org.hiahatf.mass.repo.BitcoinQuoteRepository;
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

    private BitcoinQuoteRepository bitcoinQuoteRepository;
    private Lightning lightning;
    private RateService rateService;
    private MassUtil massUtil;
    private String sendAddress;
    private Long minPay;
    private Long maxPay;

    @Autowired
    public QuoteService(
        Lightning lightning, 
        BitcoinQuoteRepository bitcoinQuoteRepository,
        @Value(Constants.SEND_ADDRESS) String sendAddress,
        @Value(Constants.MIN_PAY) Long minPay,
        @Value(Constants.MAX_PAY) Long maxPay,
        MassUtil massUtil,
        RateService rateService) {
        this.lightning = lightning;
        this.bitcoinQuoteRepository = bitcoinQuoteRepository;
        this.sendAddress = sendAddress;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.massUtil = massUtil;
        this.rateService = rateService;
    }
    
    /**
     * Get the rate and start logic for processing the 
     * Bitcoin payment request that the client proposed.
     * @param request
     * @return Mono<Quote>
     */
    public Mono<Quote> processBitcoinQuote(Request request) {
        String rate = rateService.getMoneroRate();
        Double parsedRate = massUtil.parseMoneroRate(rate);
        return decodePayReq(request, parsedRate);
    }

    /**
     * Call Lightning API for decoding the payment request
     * @param request
     * @param rate
     * @return Mono<Quote>
     */
    private Mono<Quote> decodePayReq(Request request, Double rate) {
        try {
            return lightning.decodePaymentRequest(request.getPaymentRequest())
            .flatMap(p -> {
                Double value = Double.valueOf(p.getNum_satoshis());
                // validate expiry is not set for a longer than limit
                if(Integer.valueOf(p.getExpiry()) > Constants.EXPIRY_LIMIT) {
                    return Mono.error(new MassException(Constants.EXPIRY_ERROR));
                }
                // calculate the amount of monero we expect
                Double rawAmt = value / (rate * Constants.COIN);
                Double moneroAmt = BigDecimal.valueOf(rawAmt)
                    .setScale(12, RoundingMode.HALF_UP)
                    .doubleValue();
                return finalizeQuote(value, request, p, rate, moneroAmt);
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

    /**
     * Persist the quote to the database
     * @param request
     * @param paymentRequest
     * @param rate
     * @param value
     * @param moneroAmt
     */
    private void persistQuote(Request request, PaymentRequest paymentRequest, 
    Double moneroAmount) {    
        BtcQuoteTable table = BtcQuoteTable.builder()
            .amount(moneroAmount)
            .payment_request(request.getPaymentRequest())
            .quote_id(paymentRequest.getPayment_hash())
            .refund_address(request.getRefundAddress())
            .build();
        bitcoinQuoteRepository.save(table);
    }

    /**
     * Perform any remaining work needed to process quote
     * and return to the client.
     * @param value
     * @param request
     * @param paymentRequest
     * @param rate
     * @return Mono<Quote>
     */
    private Mono<Quote> finalizeQuote(Double value, Request request, 
    PaymentRequest paymentRequest, Double rate, Double moneroAmt) {
        return massUtil.validateLiquidity(value, LiquidityType.OUTBOUND)
        .flatMap(l -> {
            if(l.booleanValue()) {
                persistQuote(request, paymentRequest, moneroAmt);
                Quote quote = Quote.builder()
                    .moneroAmt(moneroAmt)
                    .paymentRequest(request.getPaymentRequest())
                    .quoteId(paymentRequest.getPayment_hash())
                    .sendAddress(sendAddress)
                    .rate(rate)
                    .minSwapAmt(minPay)
                    .maxSwapAmt(maxPay)
                    .build();
                return Mono.just(quote);
            }
            return Mono.error(new MassException(Constants.DECODE_ERROR));
        });
    }
    
}
