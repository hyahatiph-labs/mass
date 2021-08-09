package org.hiahatf.mass.services.monero;

import java.io.IOException;

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
import org.hiahatf.mass.models.monero.transfer.TransferResponse;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;
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
    
    private MoneroQuoteRepository quoteRepository;
    private String massWalletFilename;
    private boolean isWalletOpen;
    private Lightning lightning;
    private MassUtil massUtil;
    private Monero monero;

    /**
     * Swap service dependency injection
     */
    @Autowired
    public SwapService(
        MoneroQuoteRepository quoteRepository, Lightning lightning, Monero monero,
        MassUtil massUtil, @Value(Constants.MASS_WALLET_FILENAME) String massWalletFilename) {
            this.quoteRepository = quoteRepository;
            this.massWalletFilename = massWalletFilename;
            this.lightning = lightning;
            this.massUtil = massUtil;
            this.monero = monero;
    }

     // TODO: change to /swap/initialize, and create /swap/finalize API
          // scheduler will unlock funding transaction after 20 min
          // /swap/finalize will return only fundingstate and funding unlock time if not unlocked
          // if funding  is unlocked then /swap/finalize will release multisig_txset


    public Mono<FundResponse> fundMoneroSwap(FundRequest request) {
        // reject a request that occurs during wallet operations
        if(isWalletOpen) {
            return Mono.error(new MassException(Constants.WALLET_ERROR));
        }
        XmrQuoteTable table = quoteRepository.findById(request.getHash()).get();
        isWalletOpen = true;
        return massUtil.finalizeSwapMultisig(request, table).flatMap(a -> {
            table.setSwap_address(a);
            return massUtil.exportSwapInfo(request, table).flatMap(e -> {
                return sendToConsensusWallet(table, e);
            });
        });
    }

    /**
     * Helper method for wallet control during funding of the consensus wallet.
     * @param table
     * @param fundResponse
     * @return Mono<FundResponse>
     */
    private Mono<FundResponse> sendToConsensusWallet(XmrQuoteTable table, FundResponse fundResponse) {
        return monero.controlWallet(WalletState.OPEN, massWalletFilename).flatMap(mwo -> {
            return monero.transfer(table.getSwap_address(), table.getAmount()).flatMap(t -> {
                return monero.controlWallet(WalletState.CLOSE, massWalletFilename).flatMap(mwc -> {
                    String txid = t.getResult().getTx_hash();
                    // update db
                    table.setFunding_txid(txid);
                    table.setFunding_state(FundingState.IN_PROCESS);
                    quoteRepository.save(table);
                    fundResponse.setTxid(txid);
                    return Mono.just(fundResponse);
                });
            });
        });
    }

    /**
     * Logic for processing the swap
     * 1. Verify that the lightning invoice is ACCEPTED
     * 2. Finalize the Monero Swap
     * 3. Remove the quote from db if success
     * @param SwapRequest
     * @return SwapResponse
     */
    public Mono<SwapResponse> processMoneroSwap(SwapRequest request) {
        XmrQuoteTable quote = quoteRepository.findById(request.getHash()).get();
        // verify inflight payment, state should be ACCEPTED
        try {
            return lightning.lookupInvoice(quote.getQuote_id()).flatMap(l -> {
                if(l.getState() == InvoiceState.ACCEPTED) {
                    return transferMonero(quote);
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
     * Perform Monero transfer and settle or cancel the invoice
     * @param quote
     * @return
     */
    private Mono<SwapResponse> transferMonero(XmrQuoteTable quote) {
        // TODO: sweep consensus wallet
        return monero.transfer(quote.getDest_address(), 
        quote.getAmount()).flatMap(r -> {
            // null check, since rpc since 200 on null result
            if(r.getResult() == null) {
                // monero transfer failed, cancel invoice
                return cancelMoneroSwap(quote);
            }
            return settleMoneroSwap(quote, r);
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
                    return 
                    Mono.error(
                        new MassException(Constants.SWAP_CANCELLED_ERROR)
                        );
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
    private Mono<SwapResponse> settleMoneroSwap(XmrQuoteTable quote, TransferResponse r) {
        try {
            return lightning.handleInvoice(quote, true).flatMap(c -> {
                if(c.getStatusCode() == HttpStatus.OK) {
                    // monero transfer succeeded, settle invoice
                    SwapResponse res = SwapResponse.builder()
                        .hash(quote.getQuote_id())
                        .metadata(r.getResult().getTx_metadata())
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

    // TODO: Unlock the swap wallet

    // TODO: add mediator logic

}
