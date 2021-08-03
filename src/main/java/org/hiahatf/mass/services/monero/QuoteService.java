package org.hiahatf.mass.services.monero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.LiquidityType;
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
    private boolean isWalletOpen;
    private RateService rateService;
    private Monero monero;
    private Lightning lightning;
    private MassUtil massUtil;
    private MoneroQuoteRepository quoteRepository;
    private String proofAddress;
    private Long minPay;
    private Long maxPay;

    @Autowired
    public QuoteService(RateService rateService, MassUtil massUtil, 
        Monero monero, Lightning lightning, MoneroQuoteRepository quoteRepository,
        @Value(Constants.MIN_PAY) long minPay, @Value(Constants.MAX_PAY) long maxPay,
        @Value(Constants.RP_ADDRESS) String rpAddress){
            this.rateService = rateService;
            this.massUtil = massUtil;
            this.monero = monero;
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
        isWalletOpen =false;
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
                return generateReserveProof(request, value, parsedRate);
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
                    return Mono.error(new MassException(Constants.RESERVE_PROOF_ERROR));
                }
                String hash = request.getPreimageHash();
                byte[] bHash = hash.getBytes();
                Double amount = request.getAmount();
                String address = request.getAddress();
                return validateMoneroAddress(address).flatMap(v -> { 
                    // TODO: wallet control and mulisig prep
                    // TODO: refactor to add multisig info, inject after finalizing swap multisig
                    persistQuote(address, hash, bHash, amount);
                    return generateMoneroQuote(value, address, amount, hash, bHash, rate, v, 
                        result.getSignature());
                });
        });
    }

    /**
     * Validate the Monero address that will receive the swap
     * @param address
     * @return Mono<Boolean> - true if valid
     */
    private Mono<Boolean> validateMoneroAddress(String address) {
        return monero.validateAddress(address).flatMap(r -> {
            if(!r.getResult().isValid()) {
                return Mono.error(new MassException(Constants.INVALID_ADDRESS_ERROR));
            }
            return Mono.just(true);
            });
    }

    /**
     * This method configures 2/3 multisig using multisig info passed in the request.
     * 1) Create wallet and open wallet for mass swap.
     * 2) Make multisig with clients' multisig info.
     * 3) Prepare multisig to share with mediator and client.
     * 4) Close swap wallet and perform steps 1,2 and 3 with mediator wallet.
     * 5) Finalize multisig for mass swap and mediator wallets.
     * 7) Pass prepare multisig info to final response.
     * 7) Close wallets and handle wallet control.
     * @param multisigInfo
     * @param hash
     * @return Mono<Quote>
     */
    private Mono<Quote> configureMultisig(String multisigInfo, String hash) {
        long unixTime = System.currentTimeMillis() / 1000L;
        String format = "{0}{1}";
        String swapFilename = MessageFormat.format(format, hash, unixTime);
        String mediatorFilename = MessageFormat.format(format, swapFilename, "m");
        return monero.createWallet(swapFilename).flatMap(sfn -> {
            return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwo -> {
                isWalletOpen = true;
                return monero.prepareMultisig().flatMap(spm -> {
                    return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(scwc -> {
                        isWalletOpen = false;
                        return monero.createWallet(mediatorFilename).flatMap(mfn -> {
                            return monero.controlWallet(WalletState.OPEN, mediatorFilename).flatMap(mcwo -> {
                                isWalletOpen = true;
                                List<String> infoList = Lists.newArrayList();
                                infoList.add(multisigInfo);
                                infoList.add(spm.getResult().getMultisig_info());
                                return monero.makeMultisig(infoList).flatMap(mmm -> {
                                    return monero.finalizeMultisig(infoList).flatMap(mfm -> {
                                        return monero.prepareMultisig().flatMap(mpm -> {
                                            return monero.controlWallet(WalletState.CLOSE, mediatorFilename).flatMap(mcwc -> {
                                                isWalletOpen = false;
                                                List<String> sInfoList = Lists.newArrayList();
                                                sInfoList.add(multisigInfo);
                                                sInfoList.add(mpm.getResult().getMultisig_info());
                                                return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwof -> {
                                                    isWalletOpen = true;
                                                    return monero.finalizeMultisig(sInfoList).flatMap(sfm -> {
                                                        return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(scwcf -> {
                                                            isWalletOpen = false;
                                                            // TODO: inject persist quote and generate invoice methods
                                                            // TODO: break this down to 4 methods and move to util
                                                            // TODO: create container to hold swap_address, preparemultisiginfo x2 and filenames
                                                            // 1. prepareSwapMultisig
                                                            // 2. prepareMediatorMultisig
                                                            // 3. finalizeMediatorMultisig
                                                            // 4. finalizeSwapMultisig
                                                            return null;
                                                        });
                                                    });
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
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
    private void persistQuote(String address, String hash, byte[] bHash, Double amount) {
        // store in db to settle the invoice later 
        // TODO: add wallet filenames
        XmrQuoteTable table = XmrQuoteTable.builder()
            .amount(amount).dest_address(address)
            .mediator_filename(""/* TODO: create mediator wallet flow*/)
            .payment_hash(bHash)
            .swap_address("" /* TODO: create swap address, finalize multisig*/)
            .swap_filename("" /* TODO: create swap wallet flow*/)
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
    String hash, byte[] bHash, Double rate, Boolean v, String proof) {
            ReserveProof reserveProof = ReserveProof.builder()
                .proofAddress(proofAddress).signature(proof).build();
            try {
                return lightning.generateInvoice(value, bHash).flatMap(i -> {
                    Quote quote = Quote.builder()
                        .amount(amount).quoteId(hash).destAddress(address)
                        .isValidAddress(v).invoice(i.getPayment_request())
                        .maxSwapAmt(maxPay).minSwapAmt(minPay)
                        .swapMultisigInfo("" /*TODO: generate prepare multisig info*/)
                        .mediatorMultisigInfo("" /*TODO: generate prepare multisig info*/)
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
