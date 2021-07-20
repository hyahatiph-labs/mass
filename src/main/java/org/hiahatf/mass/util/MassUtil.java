package org.hiahatf.mass.util;

import java.io.IOException;
import java.text.MessageFormat;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.services.rpc.Lightning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class MassUtil {

    private Logger logger = LoggerFactory.getLogger(MassUtil.class);
    private Double markup;
    private Lightning lightning;
    private Long minPay;
    private Long maxPay;

    /**
     * Mass utility class constructor
     * @param markup
     */
    public MassUtil(
        @Value(Constants.MARKUP) Double markup,
        @Value(Constants.MIN_PAY) long minPay,
        @Value(Constants.MAX_PAY) long maxPay,
        Lightning lightning) {
        this.markup = markup;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.lightning = lightning;
    }

    /**
     * Helper method for parsing Monero rate
     * @param rateString
     * @return Monero rate
     */
    public Double parseMoneroRate(String rateString) {
        Double parsedRate = Double
            .valueOf(rateString
            .split(Constants.SEMI_COLON_DELIMITER)[1]
            .split(Constants.RIGHT_BRACKET_DELIMITER)[0]);
        Double realRate = (parsedRate * markup) + parsedRate;
        // create the real rate by adding the markup to parsed rate
        logger.info(Constants.PARSE_RATE_MSG, parsedRate, realRate);;
        return realRate;
    }

        /**
     * Perform validations on channel balance to ensure
     * that a payment proposed on the XMR quote MAY
     * possibly be fulfilled.
     * @param value - satoshi value of invoice
     * @return Mono<Boolean>
     */
    public Mono<Boolean> validateInboundLiquidity(Double value) {
        // payment threshold validation
        long lValue = value.longValue();
        boolean isValid = lValue <= maxPay && lValue >= minPay;
        if(!isValid) {
            String error = MessageFormat.format(
                Constants.PAYMENT_THRESHOLD_ERROR, 
                String.valueOf(minPay), String.valueOf(maxPay)
                );
            return Mono.error(new MassException(error));
        }
        try {
            return lightning.fetchBalance().flatMap(b -> {
                // sum of sats in channels remote balance
                long balance = Long.valueOf(b.getRemote_balance().getSat());
                if(lValue <= balance) {
                    return Mono.just(true);
                }
                return Mono.error(new MassException(Constants.LIQUIDITY_ERROR));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

}
