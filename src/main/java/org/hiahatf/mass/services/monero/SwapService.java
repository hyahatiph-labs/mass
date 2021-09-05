package org.hiahatf.mass.services.monero;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.lightning.InvoiceState;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.SwapResponse;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.models.monero.multisig.SweepAllResponse;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling all Monero swap logic
 */
@Service(Constants.XMR_SWAP_SERVICE)
public class SwapService {
    
    private Logger logger = LoggerFactory.getLogger(SwapService.class);
    private MoneroQuoteRepository quoteRepository;
    private ScheduledExecutorService executorService = 
        Executors.newSingleThreadScheduledExecutor();
    private String massWalletFilename;
    public static boolean isWalletOpen;
    private Lightning lightning;
    private MassUtil massUtil;
    private String rpAddress;
    private Monero monero;

    /**
     * Swap service dependency injection
     */
    @Autowired
    public SwapService(
        MoneroQuoteRepository quoteRepository, Lightning lightning, Monero monero,
        MassUtil massUtil, @Value(Constants.MASS_WALLET_FILENAME) String massWalletFilename,
        @Value(Constants.RP_ADDRESS) String rpAddress) {
            this.quoteRepository = quoteRepository;
            this.massWalletFilename = massWalletFilename;
            this.lightning = lightning;
            this.rpAddress = rpAddress;
            this.massUtil = massUtil;
            this.monero = monero;
    }

    /**
     * Attempt to fund the consensus wallet.
     * @param request
     * @return
     */
    public Mono<FundResponse> fundMoneroSwap(FundRequest request) {
        if(isWalletOpen) {
            return Mono.error(new MassException(Constants.WALLET_ERROR));
        }
        XmrQuoteTable table = quoteRepository.findById(request.getHash()).get();
        isWalletOpen = true;
        return massUtil.finalizeMediatorMultisig(request).flatMap(fm -> {
            table.setSwap_address(fm.getResult().getAddress());
            return processMoneroSwap(request, table);
        });
    }

    /**
     * Logic for processing the swap
     * 1. Verify that the lightning invoice is ACCEPTED
     * 2. Start the funding transaction
     * 3. Call mediator if client tries to back out
     * @param SwapRequest
     * @return SwapResponse
     */
    public Mono<FundResponse> processMoneroSwap(FundRequest request, XmrQuoteTable table) {
        // verify inflight payment, state should be ACCEPTED
        try {
            return lightning.lookupInvoice(table.getQuote_id()).flatMap(l -> {
                if(l.getState() == InvoiceState.ACCEPTED) {
                    return sendToConsensusWallet(request, table);
                }
                return Mono.error(new MassException(Constants.OPEN_INVOICE_ERROR));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

    /**
     * Helper method for funding of the consensus wallet.
     * @param table
     * @param fundResponse
     * @return Mono<FundResponse>
     */
    private Mono<FundResponse> sendToConsensusWallet(FundRequest request, XmrQuoteTable table) {
        return monero.controlWallet(WalletState.OPEN, massWalletFilename).flatMap(mwo -> {
            return monero.transfer(table.getSwap_address(), table.getAmount()).flatMap(t -> {
                if(t.getResult() == null) {
                    return Mono.error(new MassException(Constants.FATAL_SWAP_ERROR));
                }
                return monero.controlWallet(WalletState.CLOSE, massWalletFilename).flatMap(mwc -> {
                        String txid = t.getResult().getTx_hash();
                        String quoteId = table.getQuote_id();
                        table.setFunding_txid(txid);
                        quoteRepository.save(table);
                        FundResponse fundResponse = FundResponse.builder().txid(txid).build();
                        executorService.schedule(new Mediator(quoteRepository, quoteId, 
                            lightning, monero, massUtil, rpAddress), 
                            Constants.MEDIATOR_INTERVENE_TIME, TimeUnit.SECONDS);
                        return Mono.just(fundResponse);
                });
            });
        });
    }

    /**
     * Import info from the client and export the necessary info from the swap
     * and mediator wallets. This is dependent on completion of consensus wallet
     * funding. Blocks remaining on funding transaction must be == 0;
     * @param initRequest
     * @return Mono<InitResponse> - Mediator / swap export_multisig_info 
     */
    public Mono<InitResponse> importAndExportInfo(InitRequest initRequest) {
        XmrQuoteTable table = quoteRepository.findById(initRequest.getHash()).get();
        String sfn = table.getSwap_filename();
        return monero.controlWallet(WalletState.OPEN, sfn).flatMap(o -> {
            return monero.getBalance().flatMap(b -> {
                int blocksRemaining = b.getResult().getBlocks_to_unlock();
                if(blocksRemaining != 0) {
                    String msg = MessageFormat.format(Constants.FUNDING_ERROR, 
                        String.valueOf(blocksRemaining));
                    return Mono.error(new MassException(msg));
                }
                return monero.controlWallet(WalletState.CLOSE, sfn).flatMap(c -> {
                    return massUtil.exportSwapInfo(table, initRequest);
                });
            });
        });
    }

    /**
     * Allows the client to back out of the swap within the 10 min.
     * window between funding tx unlock time and mediator intervention time.
     * @param swapRequest
     * @return Mono<SwapResponse> - empty response with HTTP OK status
     */
    public Mono<SwapResponse> processCancel(SwapRequest swapRequest) {
        isWalletOpen = true;
        XmrQuoteTable quote = quoteRepository.findById(swapRequest.getHash()).get();
        String mfn = quote.getMediator_filename();
        return monero.controlWallet(WalletState.OPEN, mfn).flatMap(o -> {
            return monero.getBalance().flatMap(b -> {
                int blocksRemaining = b.getResult().getBlocks_to_unlock();
                if(blocksRemaining != 0) {
                    String msg = MessageFormat.format(Constants.FUNDING_ERROR, blocksRemaining);
                    return Mono.error(new MassException(msg));
                }
                return monero.sweepAll(rpAddress).flatMap(r -> {
                    // null check, since rpc since 200 on null result
                    if(r.getResult() == null) {
                        logger.error(Constants.MEDIATOR_ERROR);
                    }
                    return monero.controlWallet(WalletState.CLOSE, mfn).flatMap(c -> {
                        isWalletOpen = false;
                        logger.info("Mediator sweep complete");
                        return signAndSubmitCancel(swapRequest, 
                            r.getResult().getMultisig_txset(), quote);
                    });
                });
            });
        });
    }

    /**
     * Sign and submit the multisig transaction for refund with client approval in the 
     * form of export_multisig_info. The HTLC involved is cancelled out.s
     * @param txset
     * @param quote
     * @return Mono<SwapResponse> - empty response with HTTP OK status
     */
    private Mono<SwapResponse> signAndSubmitCancel(SwapRequest swapRequest,
    String txset, XmrQuoteTable quote) {
        String sfn = quote.getSwap_filename();
        return monero.controlWallet(WalletState.OPEN, sfn).flatMap(o -> {
            return monero.signMultisig(txset).flatMap(r -> {
                // null check, since rpc since 200 on null result
                if(r.getResult() == null) {
                    logger.error(Constants.MULTISIG_CONFIG_ERROR);
                }
                return monero.submitMultisig(txset).flatMap(s -> {
                    logger.info("Cancel tx: {}", s.getResult().getTx_hash_list().get(0));
                    return monero.controlWallet(WalletState.CLOSE, sfn).flatMap(c -> {
                        logger.info("Cancel complete");
                        return cancelMoneroSwap(swapRequest, quote);
                    });
                });
            });
        });
    }

    /**
     * Perform Monero transfer and settle or cancel the invoice
     * @param quote
     * @return Mono<SwapResponse>
     */
    public Mono<SwapResponse> transferMonero(SwapRequest request) {
        XmrQuoteTable quote = quoteRepository.findById(request.getHash()).get();
        String sfn = quote.getSwap_filename();
        return monero.controlWallet(WalletState.OPEN, sfn).flatMap(mwo -> {
            return monero.getBalance().flatMap(b -> {
                int blocksRemaining = b.getResult().getBlocks_to_unlock();
                if(blocksRemaining != 0) {
                    String msg = MessageFormat.format(Constants.FUNDING_ERROR, blocksRemaining);
                    return Mono.error(new MassException(msg));
                }
                return monero.sweepAll(quote.getDest_address()).flatMap(r -> {
                    // null check, since rpc since 200 on null result
                    if(r.getResult() == null) {
                        isWalletOpen = false;
                        // monero transfer failed, cancel invoice
                        return cancelMoneroSwap(request, quote);
                    }
                    return monero.controlWallet(WalletState.CLOSE, sfn).flatMap(mwc -> {
                        isWalletOpen = false;
                        return settleMoneroSwap(request, quote, r);
                     });
                });
            });
        });
    }

    /**
     * Helper method for cancelling the Monero swap.
     * @param quote
     * @return Mono
     */
    private Mono<SwapResponse> cancelMoneroSwap(SwapRequest swapRequest, XmrQuoteTable quote) {
        isWalletOpen = false;
        try {
            return lightning.handleInvoice(swapRequest, quote, false).flatMap(c -> {
                if(c.getStatusCode() == HttpStatus.OK) {
                    quoteRepository.deleteById(quote.getQuote_id());
                    SwapResponse response = SwapResponse.builder().build();
                    return Mono.just(response);
                }
                return Mono.error(new MassException(Constants.FATAL_SWAP_ERROR));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

    /**
     * Helper method for settling the Monero swap.
     * @param quote
     * @return Mono<SwapResponse>
     */
    private Mono<SwapResponse> settleMoneroSwap(SwapRequest swapRequest, XmrQuoteTable quote, 
    SweepAllResponse sweepAllResponse) {
        try {
            return lightning.handleInvoice(swapRequest, quote, true).flatMap(c -> {
                if(c.getStatusCode() == HttpStatus.OK) {
                    executorService.shutdown();
                    // monero transfer succeeded, settle invoice
                    SwapResponse res = SwapResponse.builder()
                        .hash(quote.getQuote_id())
                        .multisigTxSet(sweepAllResponse.getResult().getMultisig_txset())
                        .build();
                    // remove quote from db
                    quoteRepository.deleteById(quote.getQuote_id());
                    return Mono.just(res);
                }
                return Mono.error(new MassException(Constants.FATAL_SWAP_ERROR));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

}
