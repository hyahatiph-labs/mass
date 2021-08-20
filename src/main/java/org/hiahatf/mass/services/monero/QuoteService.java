package org.hiahatf.mass.services.monero;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Hex;
import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.models.monero.ReserveProof;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.models.monero.proof.GetProofResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
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
 * Class for handling Monero quote logic
 */
@Service(Constants.XMR_QUOTE_SERVICE)
public class QuoteService {

    private Logger logger = LoggerFactory.getLogger(QuoteService.class);
    private MoneroQuoteRepository quoteRepository;
    private String massWalletFilename;
    private RateService rateService;
    public static boolean isWalletOpen;
    private String proofAddress;
    private Lightning lightning;
    private MassUtil massUtil;
    private Monero monero;
    private Long minPay;
    private Long maxPay;
    

    @Autowired
    public QuoteService(RateService rateService, MassUtil massUtil, 
        Monero monero, Lightning lightning, MoneroQuoteRepository quoteRepository,
        @Value(Constants.MIN_PAY) long minPay, @Value(Constants.MAX_PAY) long maxPay,
        @Value(Constants.RP_ADDRESS) String rpAddress, 
        @Value(Constants.MASS_WALLET_FILENAME) String massWalletFilename){
            this.rateService = rateService;
            this.massUtil = massUtil;
            this.monero = monero;
            this.lightning = lightning;
            this.quoteRepository = quoteRepository;
            this.minPay = minPay;
            this.maxPay = maxPay;
            this.proofAddress = rpAddress;
            this.massWalletFilename = massWalletFilename;
    }

    /**
     * Method for building the monero quote
     * and returning it to the client
     * @param request - quote request from client
     * @return Mono<MoneroQuote>
     */
    public Mono<Quote> processMoneroQuote(Request request) {
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
                    return generateReserveProof(request, value, parsedRate);
                });
            }
            logger.error(Constants.UNK_ERROR);
            // edge case, this should never happen...
            return Mono.error(new MassException(Constants.UNK_ERROR));
        });
    }

    /**
     * Call Monero RPC to generate the reserve proof.
     * Afterwards validate the Monero address, persist the quote to
     * the database and return the quote as requested.
     * @param request
     * @param rawRate
     * @return Mono<Quote>
     */
    private Mono<Quote> generateReserveProof(Request request, Double value, Double rate) {
        return monero.getReserveProof(request.getAmount()).flatMap(r -> {
            GetProofResult result = r.getResult();
            if(result == null) {
                isWalletOpen = false;
                return Mono.error(new MassException(Constants.RESERVE_PROOF_ERROR));
            }
            return validateMoneroAddress(request, value, rate, r.getResult().getSignature());
        });
    }

    /**
     * Validate the Monero address that will receive the swap
     * @param address
     * @return Mono<Quote>
     */
    private Mono<Quote> validateMoneroAddress(Request request, Double value, Double rate,
    String s) {
        String hash = Hex.encodeHexString(request.getPreimageHash());
        byte[] bHash = request.getPreimageHash();
        Double amount = request.getAmount();
        String address = request.getAddress();
        return monero.validateAddress(address).flatMap(r -> {
            if(!r.getResult().isValid()) {
                return Mono.error(new MassException(Constants.INVALID_ADDRESS_ERROR));
            }
            logger.info("Entering Multisig Setup");
            return monero.controlWallet(WalletState.CLOSE, massWalletFilename).flatMap(mwo -> {
                return massUtil.configureMultisig(request.getMultisigInfo(), hash).flatMap(m -> {
                    logger.info("Multisig setup complete");
                    persistQuote(address, hash, bHash, amount, m);
                    return generateMoneroQuote(value, address, amount, hash, bHash, rate, s, m);
                });
            });
        });
    }

    /**
     * Persist the MoneroQuote to the database for future processing.
     * @param address - destination address
     * @param hash - preimage hash
     * @param bHash - byte array of preimage hash
     * @param amount - amount of Monero
     */
    private void persistQuote(String address, String hash, byte[] bHash, 
    Double amount, MultisigData data) {
        // store in db to settle the invoice later 
        XmrQuoteTable table = XmrQuoteTable.builder()
            .amount(amount).dest_address(address)
            .mediator_filename(data.getMediatorFilename())
            .mediator_finalize_msig(data.getMediatorFinalizeMultisigInfo())
            .swap_finalize_msig(data.getSwapFinalizeMultisigInfo())
            .payment_hash(bHash)
            .swap_filename(data.getSwapFilename())
            .quote_id(hash)
            .build();
        quoteRepository.save(table);
    }

    /**
     * Helper function for generating the Monero quote
     * Uses the LND addholdinvoice API call.
     * @param value - amount of quote in satoshis
     * @param address - destination address of the swap
     * @param amount - amount of swap in monero
     * @param hash - preimage hash
     * @param bHash - byte array of preimage hash
     * @param rate - rate with fee
     * @param v - boolean of address validation
     * @param proof - result from generating the reserve proof
     * @return Mono<MoneroQuote>
     */
    private Mono<Quote> generateMoneroQuote(Double value, String address, Double amount, 
    String hash, byte[] bHash, Double rate, String proof, MultisigData data) {
        isWalletOpen = false;
        ReserveProof reserveProof = ReserveProof.builder()
            .proofAddress(proofAddress).signature(proof).build();
        try {
            return lightning.generateInvoice(value, bHash).flatMap(i -> {
                Quote quote = Quote.builder()
                    .amount(amount).quoteId(hash).destAddress(address)
                    .maxSwapAmt(maxPay).minSwapAmt(minPay).invoice(i.getPayment_request())
                    .swapMakeMultisigInfo(data.getSwapMakeMultisigInfo())
                    .mediatorMakeMultisigInfo(data.getMediatorMakeMultisigInfo())
                    .swapFinalizeMultisigInfo(data.getSwapFinalizeMultisigInfo())
                    .mediatorFinalizeMultisigInfo(data.getSwapFinalizeMultisigInfo())
                    .rate(rate).reserveProof(reserveProof)
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
