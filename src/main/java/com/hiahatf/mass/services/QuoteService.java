package com.hiahatf.mass.services;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.UUID;

import javax.net.ssl.SSLException;

import com.hiahatf.mass.exception.MassException;
import com.hiahatf.mass.models.MoneroQuote;
import com.hiahatf.mass.models.MoneroRequest;
import com.hiahatf.mass.services.rate.RateService;
import com.hiahatf.mass.services.rpc.Lightning;
import com.hiahatf.mass.services.rpc.Monero;
import com.hiahatf.mass.util.MassUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service("QuoteService")
public class QuoteService {

    private Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private static final String INVALID_ADDRESS = "Invalid address";
    private static final String KECCAK_256 = "Keccak-256";
    private static final Long COIN = 100000000L;
    private RateService rateService;
    private Monero moneroRpc;
    private Lightning lightning;
    private MassUtil massUtil;

    @Autowired
    public QuoteService(RateService rateService, MassUtil massUtil, 
        Monero moneroRpc, Lightning lightning) {
            this.rateService = rateService;
            this.massUtil = massUtil;
            this.moneroRpc = moneroRpc;
            this.lightning = lightning;
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
                Double rate = massUtil.parseMoneroRate(r);
                Double value = (rate * request.getAmount()) * COIN;
                // store in db to settle the invoice later
                byte[] bPreimage = createPreimage();
                logger.info("preimage: {}", bPreimage);
                int[] hash = createPreimageHash(bPreimage);
                logger.info("preimage hash: {}", hash);
                // TODO: save quote to db with status
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
     * @return Mono<MoneroQuote>
     */
    private Mono<MoneroQuote> generateMoneroQuote(Double value, int[] hash,
        MoneroRequest request, Double rate, Boolean v) {
        String quoteId = UUID.randomUUID().toString();
        try {
            return lightning.generateInvoice(value, hash).flatMap(i -> {
                MoneroQuote quote = MoneroQuote.builder()
                    .quoteId(quoteId)
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
                    return Mono.error(new MassException(INVALID_ADDRESS));
                }
                return Mono.just(true);
            });
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
     * @return int[] - using int[] since byte array is unsigned
     */
    private int[] createPreimageHash(byte[] preimage) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(KECCAK_256);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Preimage hashing error: {}", e.getMessage());
        }     
        // Java doesn't support uint[] (T_T), do the conversion
        byte[] hash = digest.digest(preimage);
        int[] iHash = new int[32];
        for(int i = 0; i < hash.length; i++) {
            iHash[i] = (hash[i] & 0xFF);
        }
        return iHash;
    }

}
