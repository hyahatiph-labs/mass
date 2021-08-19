package org.hiahatf.mass.services.monero;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.FundingState;
import org.hiahatf.mass.models.lightning.InvoiceState;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
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
        if(table.getFunding_state() != FundingState.PENDING) {
            return Mono.error(new MassException(Constants.FUNDING_ERROR));
        }
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

    // TODO: manually sweep latest multisig wallet

    // TODO: test these changes for export info multisig failure due to SQL column length

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
                return massUtil.exportSwapInfo(request, table).flatMap(fundResponse -> {
                    return monero.controlWallet(WalletState.CLOSE, massWalletFilename).flatMap(mwc -> {
                        String txid = t.getResult().getTx_hash();
                        String quoteId = table.getQuote_id();
                        table.setFunding_txid(txid);
                        table.setFunding_state(FundingState.IN_PROCESS);
                        quoteRepository.save(table);
                        fundResponse.setTxid(txid);
                        long unlockTime = (System.currentTimeMillis() / 1000L) + 
                            Constants.FUNDING_LOCK_TIME;
                        logger.info("Funding unlock executor scheduled for {}", unlockTime);
                        executorService.schedule(new UnlockFunding(quoteId, quoteRepository), 
                            Constants.FUNDING_LOCK_TIME, TimeUnit.SECONDS);
                        executorService.schedule(new Mediator(quoteRepository, quoteId, lightning), 
                            Constants.MEDIATOR_INTERVENE_TIME, TimeUnit.SECONDS);
                        fundResponse.setUnlockTime(unlockTime);
                        return Mono.just(fundResponse);
                    });
                });
            });
        });
    }

    public Mono<SwapResponse> processCancel(SwapRequest swapRequest) {
        isWalletOpen = true;
        XmrQuoteTable quote = quoteRepository.findById(swapRequest.getHash()).get();
        String mfn = quote.getMediator_filename();
        return massUtil.importSwapInfo(swapRequest, quote).flatMap(i -> {
            if(i.getResult().getN_outputs() <= 0) {
                return Mono.error(new MassException(Constants.MULTISIG_CONFIG_ERROR));
            }
            return monero.controlWallet(WalletState.OPEN, mfn).flatMap(o -> {
                return monero.sweepAll(rpAddress).flatMap(r -> {
                    // null check, since rpc since 200 on null result
                    if(r.getResult() == null) {
                        logger.error(Constants.MEDIATOR_ERROR);
                    }
                    return monero.controlWallet(WalletState.CLOSE, mfn).flatMap(c -> {
                        logger.info("Mediator sweep complete");
                        return signAndSubmitCancel(r.getResult().getMultisig_txset(), quote);
                    });
                });
            });
        });
    }

    private Mono<SwapResponse> signAndSubmitCancel(String txset, XmrQuoteTable quote) {
        return monero.controlWallet(WalletState.OPEN, massWalletFilename).flatMap(o -> {
            return monero.signMultisig(txset).flatMap(r -> {
                // null check, since rpc since 200 on null result
                if(r.getResult() == null) {
                    logger.error(Constants.MULTISIG_CONFIG_ERROR);
                }
                return monero.submitMultisig(txset).flatMap(s -> {
                    logger.info("Cancel tx: {}", s.getResult().getTx_hash_list().get(0));
                    return monero.controlWallet(WalletState.CLOSE, massWalletFilename).flatMap(c -> {
                        isWalletOpen = false;
                        logger.info("Cancel complete");
                        return cancelMoneroSwap(quote);
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
        if(quote.getFunding_state() != FundingState.COMPLETE) {
            return Mono.error(new MassException(Constants.FUNDING_ERROR));
        }
        executorService.shutdown();
        return monero.controlWallet(WalletState.OPEN, massWalletFilename).flatMap(mwo -> {
            return monero.sweepAll(quote.getDest_address()).flatMap(r -> {
                // null check, since rpc since 200 on null result
                if(r.getResult() == null) {
                    isWalletOpen = false;
                    // monero transfer failed, cancel invoice
                    return cancelMoneroSwap(quote);
                }
                return monero.controlWallet(WalletState.CLOSE, massWalletFilename).flatMap(mwc -> {
                    isWalletOpen = false;
                    return settleMoneroSwap(quote, r);
                });
            });
        });
    }

    /**
     * Helper method for cancelling the Monero swap.
     * @param quote
     * @return Mono
     */
    private Mono<SwapResponse> cancelMoneroSwap(XmrQuoteTable quote) {
        try {
            return lightning.handleInvoice(quote, false).flatMap(c -> {
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
    private Mono<SwapResponse> settleMoneroSwap(XmrQuoteTable quote, 
    SweepAllResponse sweepAllResponse) {
        try {
            return lightning.handleInvoice(quote, true).flatMap(c -> {
                if(c.getStatusCode() == HttpStatus.OK) {
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
