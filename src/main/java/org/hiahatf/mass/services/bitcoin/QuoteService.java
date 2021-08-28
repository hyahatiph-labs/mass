package org.hiahatf.mass.services.bitcoin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.bitcoin.BtcQuoteTable;
import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.models.lightning.PaymentRequest;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.BitcoinQuoteRepository;
import org.hiahatf.mass.services.rate.RateService;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling Bitcoin quote logic
 */
@Service(Constants.BTC_QUOTE_SERVICE)
public class QuoteService {

    private Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private BitcoinQuoteRepository bitcoinQuoteRepository;
    public static boolean isWalletOpen;
    private String massWalletFilename;
    private RateService rateService;
    private String sendAddress;
    private MassUtil massUtil;
    private Monero monero;
    private Long minPay;
    private Long maxPay;

    @Autowired
    public QuoteService(BitcoinQuoteRepository bitcoinQuoteRepository,
    @Value(Constants.SEND_ADDRESS) String sendAddress, @Value(Constants.MIN_PAY) Long minPay,
    @Value(Constants.MAX_PAY) Long maxPay, MassUtil massUtil, RateService rateService,
    @Value(Constants.MASS_WALLET_FILENAME) String massWalletFilename, Monero monero) {
        this.bitcoinQuoteRepository = bitcoinQuoteRepository;
        this.massWalletFilename = massWalletFilename;
        this.sendAddress = sendAddress;
        this.rateService = rateService;
        this.massUtil = massUtil;
        this.monero = monero;
        this.minPay = minPay;
        this.maxPay = maxPay;
    }
    
    /**
     * Get the rate and configure multisig wallet.
     * @param request
     * @return Mono<Quote>
     */
    public Mono<Quote> processBitcoinQuote(Request request) {
        // reject a request that occurs during wallet operations
        if(isWalletOpen) {
            return Mono.error(new MassException(Constants.WALLET_ERROR));
        }
        String rate = rateService.getMoneroRate();
        Double parsedRate = massUtil.parseMoneroRate(rate);
        Double value = (parsedRate * request.getAmount()) * Constants.COIN;
        /*  
         * The quote amount is validated before a response is sent.
         * Minimum and maximum payments are configured via the MASS
         * application.yml. There is no limit on requests. The amount
         * is also validated with Monero reserve proof.
         */
        return massUtil.validateLiquidity(value, LiquidityType.INBOUND).flatMap(l -> {
            if(l.booleanValue()) {
                return monero.controlWallet(WalletState.OPEN, massWalletFilename).flatMap(mwo -> {
                    isWalletOpen = true;
                    // TODO: refactor this to chaining quote logic to a verification
                    // of the Monero reserve proof
                    return Mono.just(Quote.builder().build());
                });
            }
            logger.error(Constants.UNK_ERROR);
            // edge case, this should never happen...
            return Mono.error(new MassException(Constants.UNK_ERROR));
        });
    }

    // TODO: move this to swap, will decode payment request before handling
    // handling the txset and revealing the preimage

    // /**
    //  * Call Lightning API for decoding the payment request
    //  * @param request
    //  * @param rate
    //  * @return Mono<Quote>
    //  */
    // private Mono<Quote> decodePayReq(Request request, Double rate) {
    //     try {
    //         return lightning.decodePaymentRequest(request.getPaymentRequest()).flatMap(p -> {
    //             Double value = Double.valueOf(p.getNum_satoshis());
    //             // validate expiry is not set for a longer than limit
    //             if(Integer.valueOf(p.getExpiry()) > Constants.EXPIRY_LIMIT) {
    //                 return Mono.error(new MassException(Constants.EXPIRY_ERROR));
    //             }
    //             // calculate the amount of monero we expect
    //             Double rawAmt = value / (rate * Constants.COIN);
    //             Double moneroAmt = BigDecimal.valueOf(rawAmt)
    //                 .setScale(12, RoundingMode.HALF_UP)
    //                 .doubleValue();
    //             return finalizeQuote(value, request, p, rate, moneroAmt);
    //         });
    //     } catch (SSLException se) {
    //         return Mono.error(new MassException(se.getMessage()));
    //     } catch (IOException ie) {
    //         return Mono.error(new MassException(ie.getMessage()));
    //     }
    // }

    /**
     * Persist the quote to the database
     * @param request
     * @param paymentRequest
     * @param rate
     * @param value
     * @param moneroAmt
     */
    private void persistQuote(Request request, PaymentRequest paymentRequest, Double moneroAmount) {
        // TODO: persist multisig data   
        BtcQuoteTable table = BtcQuoteTable.builder()
            .amount(moneroAmount)
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
        return massUtil.validateLiquidity(value, LiquidityType.OUTBOUND).flatMap(l -> {
            if(l.booleanValue()) {
                persistQuote(request, paymentRequest, moneroAmt);
                Quote quote = Quote.builder()
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

}
