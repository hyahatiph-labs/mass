package org.hiahatf.mass.services.monero;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.peer.Peer;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.repo.PeerRepository;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable triggered by the ScheduledExecutor for mediating
 * the consensus wallet for unresolvable swaps in order to
 * prevent mlof (mass loss of funds).
 */
public class Mediator implements Runnable {

    private Logger logger = LoggerFactory.getLogger(Mediator.class);
    private ScheduledExecutorService executorService = 
        Executors.newSingleThreadScheduledExecutor();
    private MoneroQuoteRepository quoteRepository;
    private PeerRepository peerRepository;
    private int retryCounter;
    private String refundAddress;
    private MassUtil massUtil;
    private String quoteId;
    private Monero monero;
    
    /**
    * After the 60 minute consensus wallet time for swap finality
    * revert the funds back to the mass wallet. Invoices are generated
    * with a 7200 expiry for grace period of swap. This swap is no longer
    * valid and is removed from the database.
    * @param quoteId
    */
    public Mediator(MoneroQuoteRepository quoteRepository, String quoteId,
    Monero monero, MassUtil massUtil, String refundAddress, int retryCounter,
    PeerRepository peerRepository) {
        this.quoteRepository = quoteRepository;
        this.peerRepository = peerRepository;
        this.refundAddress = refundAddress;
        this.retryCounter = retryCounter;
        this.massUtil = massUtil;
        this.quoteId = quoteId;
        this.monero = monero;
    }

    /**
     * Calls upon the mediator to provide export_multisig_info and sweep
     * for the quote in question. The mass application then signs and 
     * submits the multisig transaction for the intervention.
     */
    public void run() {
        MoneroQuote quote = quoteRepository.findById(quoteId).get();
        logger.info("Executing mediator for swap {}", quote.getQuote_id());
        SwapService.isWalletOpen = true;
        String mfn = quote.getMediator_filename();
        InitRequest mediatorRequest = InitRequest.builder()
            .importInfo(Constants.MEDIATOR_CHECK).build();
        massUtil.exportSwapInfo(quote, mediatorRequest).subscribe(i -> {
            monero.controlWallet(WalletState.OPEN, mfn).subscribe(o -> {
                 monero.sweepAll(refundAddress).subscribe(r -> {
                    // null check, since rpc since 200 on null result
                    if(r.getResult() == null) {
                        // recursive retry logic for the mediator
                        logger.info("Mediator retry: {}", retryCounter);
                        retryCounter++;
                        if(retryCounter < 3) {
                            executorService.schedule(new Mediator(quoteRepository, quoteId, monero, 
                            massUtil, refundAddress, retryCounter, peerRepository), 
                            Constants.MEDIATOR_RETRY_DELAY, TimeUnit.SECONDS);
                        }
                        logger.error(Constants.MEDIATOR_ERROR);
                    } else {
                        monero.controlWallet(WalletState.CLOSE, mfn).subscribe(c -> {
                            logger.info("Mediator sweep complete");
                            signAndSubmitCancel(r.getResult().getMultisig_txset(), quote);
                        });
                    }
                });
            });
        });
    }

    /**
     * Helper method for mediator xmr wallet recovery of funds.
     * @param txset
     * @param quote
     */
    private void signAndSubmitCancel(String txset, MoneroQuote quote) {
        logger.info("Signing mediator intervention");
        String sfn = quote.getSwap_filename();
         monero.controlWallet(WalletState.OPEN, sfn).subscribe(o -> {
             monero.signMultisig(txset).subscribe(r -> {
                // null check, since rpc since 200 on null result
                if(r.getResult() == null) {
                    logger.error(Constants.MULTISIG_CONFIG_ERROR);
                }
                 monero.submitMultisig(r.getResult().getTx_data_hex()).subscribe(s -> {
                    logger.info("Cancel tx: {}", s.getResult().getTx_hash_list().get(0));
                    monero.controlWallet(WalletState.CLOSE, sfn).subscribe(c -> {
                        SwapService.isWalletOpen = false;
                        Peer peer = peerRepository.findById(quote.getPeer_id()).get();
                        int cancels = peer.getCancel_counter() + 1;
                        Peer updatePeer = Peer.builder().cancel_counter(cancels).build();
                        peerRepository.save(updatePeer);
                        logger.info("Cancel complete");
                    });
                });
            });
        });
    }
    
}
