package org.hiahatf.mass.services.monero;

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
import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.models.monero.ReserveProof;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.rate.RateService;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling Monero quote logic
 */
@Service(Constants.XMR_QUOTE_SERVICE)
public class QuoteService {

    private Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private RateService rateService;
    private Monero moneroRpc;
    private Lightning lightning;
    private MassUtil massUtil;
    private MoneroQuoteRepository quoteRepository;
    private String proofAddress;
    private Long minPay;
    private Long maxPay;

    @Autowired
    public QuoteService(RateService rateService, MassUtil massUtil, 
        Monero moneroRpc, Lightning lightning, MoneroQuoteRepository quoteRepository,
        @Value(Constants.MIN_PAY) long minPay,
        @Value(Constants.MAX_PAY) long maxPay,
        @Value(Constants.RP_ADDRESS) String rpAddress){
            this.rateService = rateService;
            this.massUtil = massUtil;
            this.moneroRpc = moneroRpc;
            this.lightning = lightning;
            this.quoteRepository = quoteRepository;
            this.minPay = minPay;
            this.maxPay = maxPay;
            this.proofAddress = rpAddress;
    }

    /**
     * Method for building the monero quote
     * and returning it to the client
     * @param request - quote request from client
     * @return Mono<MoneroQuote>
     */
    public Mono<Quote> processMoneroQuote(Request request) {
        String rate = rateService.getMoneroRate();
        Double parsedRate = massUtil.parseMoneroRate(rate);
        Double value = (parsedRate * request.getAmount()) * Constants.COIN;
        /*  
         * The quote amount is validated before a response is sent.
         * Minimum and maximum payments are configured via the MASS
         * application.yml. There is no limit on requests. The amount
         * is also validated with Monero reserve proof.
         */
        return massUtil.validateLiquidity(value, LiquidityType.INBOUND)
        .flatMap(l -> {
            if(l.booleanValue()) {
                return generateReserveProof(request, value, parsedRate);
            }
            // edge case, this should never happen...
            return Mono.error(new MassException(Constants.UNK_ERROR));
        });
    }

    /**
     * Call Monero RPC to generate the reserve proof. Also call
     * some helper methods for generating the preimage and hash.
     * Finally validate the Monero address, persist the quote to
     * the database and return the quote as requested.
     * @param request
     * @param rawRate
     * @return Mono<Quote>
     */
    private Mono<Quote> generateReserveProof(Request request, 
    Double value, Double rate) {
        return moneroRpc.getReserveProof(request.getAmount()).flatMap(r -> {
                if(r.getResult() == null) {
                    return Mono.error(
                        new MassException(Constants.RESERVE_PROOF_ERROR)
                        );
                }
                return validateMoneroAddress(request.getAddress()).flatMap(v -> { 
                    byte[] preimage = createPreimage();
                    byte[] hash = createPreimageHash(preimage);
                    persistQuote(request, preimage, hash);

                    // TODO: wallet control and mulisig prep

                    // TODO: refactor to add multisig info
                    return generateMoneroQuote(value, hash, request, rate, v, 
                        r.getResult().getSignature());
                });
        });
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
                        new MassException(Constants.INVALID_ADDRESS_ERROR));
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
     * @return byte[]
     */
    private byte[] createPreimageHash(byte[] preimage) {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(Constants.SHA_256);
        } catch (NoSuchAlgorithmException e) {
            logger.error(Constants.HASH_ERROR, e.getMessage());
        }     
        return digest.digest(preimage);
    }

    /**
     * Persist the MoneroQuote to the database for future processing.
     * @param request
     * @param preimage
     * @param hash
     */
    private void persistQuote(Request request, byte[] preimage, 
    byte[] hash) {
        // store in db to settle the invoice later

        // TODO: add wallet filename
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
     * Helper function for generating the Monero quote
     * Uses the LND addholdinvoice API call.
     * @param value - amount of quote in satoshis
     * @param hash - preimage hash
     * @param request - request from client
     * @param rate - rate with fee
     * @param v - boolean of address validation
     * @param quoteId - id to recover quote for future reference
     * @return Mono<MoneroQuote>
     */
    private Mono<Quote> generateMoneroQuote(Double value, byte[] hash,
        Request request, Double rate, Boolean v, String proof) {
            ReserveProof reserveProof = ReserveProof.builder()
                .proofAddress(proofAddress)
                .signature(proof).build();
            try {
                return lightning.generateInvoice(value, hash).flatMap(i -> {
                    Quote quote = Quote.builder()
                        .quoteId(Hex.encodeHexString(hash))
                        .address(request.getAddress())
                        .isValidAddress(v)
                        .amount(request.getAmount())
                        .invoice(i.getPayment_request())
                        .reserveProof(reserveProof)
                        .rate(rate)
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
    }

}
