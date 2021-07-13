package org.hiahatf.mass.services;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.hiahatf.mass.models.monero.MoneroRequest;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.repo.QuoteRepository;
import org.hiahatf.mass.services.rate.RateService;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling quote logic
 */
@Service
public class QuoteService {

    private Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private RateService rateService;
    private Monero moneroRpc;
    private Lightning lightning;
    private MassUtil massUtil;
    private QuoteRepository quoteRepository;
    private long minPay;
    private long maxPay;

    @Autowired
    public QuoteService(
        RateService rateService, 
        MassUtil massUtil, 
        Monero moneroRpc, 
        Lightning lightning, 
        QuoteRepository quoteRepository,
        @Value(Constants.MIN_PAY) long minPay,
        @Value(Constants.MAX_PAY) long maxPay) {
            this.rateService = rateService;
            this.massUtil = massUtil;
            this.moneroRpc = moneroRpc;
            this.lightning = lightning;
            this.quoteRepository = quoteRepository;
            this.minPay = minPay;
            this.maxPay = maxPay;
    }

    /**
     * Method for building the monero quote
     * and returning it to the client
     * @return Mono<MoneroQuote>
     */
    public Mono<MoneroQuote> processMoneroQuote(MoneroRequest request) {
        return rateService.getMoneroRate().flatMap(r -> {
            // validate the address

            // TODO: inject into quote validation, move quote validation here
            return validateMoneroAddress(request.getAddress()).flatMap(v -> {
                Double rate = massUtil.parseMoneroRate(r);
                Double value = (rate * request.getAmount()) * Constants.COIN;
                byte[] preimage = createPreimage();
                byte[] hash = createPreimageHash(preimage);
                persistQuote(request, preimage, hash);
                return generateMoneroQuote(value, hash, request, rate, v);
            });   
        });
    }

    /**
     * Helper function for generating the Monero quote
     * @param value - amount of quote in satoshis
     * @param hash - preimage hash
     * @param request - request from client
     * @param rate - rate with fee
     * @param v - boolean of address validation
     * @param quoteId - id to recover quote for future reference
     * @return Mono<MoneroQuote>
     */
    private Mono<MoneroQuote> generateMoneroQuote(Double value, byte[] hash,
        MoneroRequest request, Double rate, Boolean v) {
            try {
                return lightning.generateInvoice(value, hash).flatMap(i -> {
                    MoneroQuote quote = MoneroQuote.builder()
                        .quoteId(Hex.encodeHexString(hash))
                        .address(request.getAddress())
                        .isValidAddress(v)
                        .amount(request.getAmount())
                        .invoice(i.getPayment_request())
                        .rate(rate)
                        .build();
                    return Mono.just(quote);
                });
            } catch (SSLException se) {
                return Mono.error(new MassException(se.getMessage()));
            } catch (IOException ie) {
                return Mono.error(new MassException(ie.getMessage()));
            }
    }

    /**
     * Validate the Monero address that will receive the swap
     * @param address
     * @return Mono<Boolean> - true if valid
     */
    private Mono<Boolean> validateMoneroAddress(String address) {
        return moneroRpc.validateAddress(address).flatMap(r -> {
                if(!r.getResult().isValid()) {
                    return Mono.error(
                        new MassException(Constants.INVALID_ADDRESS))
                    ;
                }
                return Mono.just(true);
            });
    }

    /**
     * Perform validations on channel balance to ensure
     * that a payment proposed on the XMR quote MAY
     * possibly be fulfilled.
     * @param value
     * @return
     */
    private Mono<Boolean> validateInboundLiquidity(Double value) {
        try {
            return lightning.fetchBalance().flatMap(b -> {
                // sum of sats in channels remote balance
                long balance = Long.valueOf(b.getRemote_balance().getSat());
                // satoshi value on the invoice
                long sValue = value.longValue();
                if(sValue <= balance) {
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

    /**
     * Create the 32 byte preimage
     * @return byte[]
     */
    private byte[] createPreimage() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[32];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Create the 32 byte preimage hash
     * @param preimage
     * @return byte[]
     */
    private byte[] createPreimageHash(byte[] preimage) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(Constants.SHA_256);
        } catch (NoSuchAlgorithmException e) {
            logger.error(Constants.HASH_ERROR_MSG, e.getMessage());
        }     
        return digest.digest(preimage);
    }

    /**
     * Persist the MoneroQuote to the database for future processing.
     * @param request
     * @param preimage
     * @param hash
     */
    private void persistQuote(MoneroRequest request, byte[] preimage, byte[] hash) {
        // store in db to settle the invoice later
        XmrQuoteTable table = XmrQuoteTable.builder()
            .xmr_address(request.getAddress())
            .amount(request.getAmount())
            .preimage(preimage)
            .payment_hash(hash)
            .quote_id(Hex.encodeHexString(hash))
            .build();
        quoteRepository.save(table);
    }

    /**
     * The quote amount is validated before a response is sent.
     * Minimum and maximum payments are configured via the MASS
     * application.yml. There is no limit on requests. The amount
     * is also validated with Monero reserve proof.
     * @param amount
     * @return Mono<MoneroQuote>
     */
    private Mono<MoneroQuote> validateQuote(Double amount) {
        // TODO: inject and build on the validate address logic
        //boolean isValidAmt = amount.longValue() <= 
        // validate min. and max. MASS payments in satoshis
        

        // validate channel liquidity

        // validate Monero reserves proof

        return Mono.just(MoneroQuote.builder().build());
    }

}
