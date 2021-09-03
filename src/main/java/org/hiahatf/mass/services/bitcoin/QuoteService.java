package org.hiahatf.mass.services.bitcoin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.bitcoin.BtcQuoteTable;
import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.proof.CheckReserveProofResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.BitcoinQuoteRepository;
import org.hiahatf.mass.services.rate.RateService;
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
    @Value(Constants.MIN_PAY) Long minPay, @Value(Constants.MAX_PAY) Long maxPay, 
    MassUtil massUtil, RateService rateService, Monero monero, @Value(Constants.SEND_ADDRESS) 
    String sendAddress, @Value(Constants.MASS_WALLET_FILENAME) String massWalletFilename) {
        this.bitcoinQuoteRepository = bitcoinQuoteRepository;
        this.massWalletFilename = massWalletFilename;
        this.rateService = rateService;
        this.sendAddress = sendAddress;
        this.massUtil = massUtil;
        this.monero = monero;
        this.minPay = minPay;
        this.maxPay = maxPay;
    }
    
    /**
     * Get the rate and configure multisig wallet.
     * 
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
                    return checkReserveProof(request, parsedRate);
                });
            }
            logger.error(Constants.UNK_ERROR);
            // edge case, this should never happen...
            return Mono.error(new MassException(Constants.UNK_ERROR));
        });
    }

    /**
     * Call Monero RPC to validate the reserve proof.
     * Afterwards validate the Monero refund address, persist the quote to
     * the database and return the quote as requested.
     * 
     * @param request
     * @param rate
     * @return Mono<Quote>
     */
    private Mono<Quote> checkReserveProof(Request request, Double rate) {
        return monero.checkReserveProof(request.getProofAddress(), request.getProofSignature())
            .flatMap(rp -> {
            CheckReserveProofResult checked = rp.getResult();
            if(rp == null || !checked.isGood() || checked.getTotal()
               < (request.getAmount() * Constants.PICONERO)) {
                return Mono.error(new MassException(Constants.RESERVE_PROOF_ERROR));
            }
            return validateMoneroAddress(request, rate);
        });
    }

    /**
     * Validate the Monero address that will receive the swap
     * 
     * @param request
     * @param rate
     * @return Mono<Quote>
     */
    private Mono<Quote> validateMoneroAddress(Request request, Double rate) {
        byte[] preimage = createPreimage();
        byte[] preimageHash = createPreimageHash(preimage);
        String hexHash = Hex.encodeHexString(preimageHash);
        String address = request.getRefundAddress();
        return monero.validateAddress(address).flatMap(r -> {
            if(!r.getResult().isValid()) {
                return Mono.error(new MassException(Constants.INVALID_ADDRESS_ERROR));
            }
            logger.info("Entering Multisig Setup");
            return monero.controlWallet(WalletState.CLOSE, massWalletFilename).flatMap(mwo -> {
                return massUtil.rConfigureMultisig(request.getSwapMultisigInfos(), hexHash).flatMap(m -> {
                    logger.info("Multisig setup complete");
                    persistQuote(request, m, preimage, preimageHash, hexHash);
                    return finalizeQuote(request, m, rate, hexHash);
                });
            });
        });
    }

    /**
     * Create the 32 byte preimage
     * 
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
     * 
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
     * Persist the quote to the database
     * 
     * @param request - request from client
     * @param data - multisig configuration data
     * @param preimage - 32-byte array of preimage
     * @param hash - 32-byte array of preimage hash
     * @param hexHash - hex-encoding of the 32-byte array preimage
     */
    private void persistQuote(Request request, MultisigData data, byte[] preimage, 
    byte[] hash, String hexHash) {
        BtcQuoteTable table = BtcQuoteTable.builder()
            .amount(request.getAmount())
            .preimage(preimage)
            .payment_hash(hash)
            .quote_id(hexHash)
            .refund_address(request.getRefundAddress())
            .swap_filename(data.getSwapFilename())
            .build();
        bitcoinQuoteRepository.save(table);
    }

    /**
     * Perform any remaining work needed to process quote
     * and return to the client.
     * 
     * @param request - request from client
     * @param data - multisig configuration data
     * @param rate - rate for swap, includes Mass markup / premium
     * @param hash - hex-encoding of the 32-byte array preimage
     * @return Mono<Quote>
     */
    private Mono<Quote> finalizeQuote(Request request, MultisigData data, Double rate, String hash) {
        Quote quote = Quote.builder()
            .quoteId(hash)
            .amount(request.getAmount())
            .refundAddress(request.getRefundAddress())
            .rate(rate)
            .minSwapAmt(minPay)
            .maxSwapAmt(maxPay)
            .sendTo(sendAddress)
            .swapMakeMultisigInfo(data.getSwapMakeMultisigInfo())
            .swapFinalizeMultisigInfo(data.getSwapFinalizeMultisigInfo())
            .build();
        return Mono.just(quote);
    }

}
