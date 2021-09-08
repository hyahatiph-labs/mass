package org.hiahatf.mass.services.monero;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
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
    private MoneroQuoteRepository quoteRepository;
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
    Monero monero, MassUtil massUtil, String refundAddress) {
        this.quoteRepository = quoteRepository;
        this.refundAddress = refundAddress;
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
        XmrQuoteTable table = quoteRepository.findById(quoteId).get();
        SwapService.isWalletOpen = true;
        String mfn = table.getMediator_filename();
        InitRequest mediatorRequest = InitRequest.builder()
            .importInfo(Constants.MEDIATOR_CHECK).build();
        monero.controlWallet(WalletState.OPEN, mfn).subscribe(o -> {
            massUtil.exportSwapInfo(table, mediatorRequest).subscribe(i -> {
                 monero.sweepAll(refundAddress).subscribe(r -> {
                    // null check, since rpc since 200 on null result
                    if(r.getResult() == null) {
                        logger.error(Constants.MEDIATOR_ERROR);
                    }
                     monero.controlWallet(WalletState.CLOSE, mfn).subscribe(c -> {
                        logger.info("Mediator sweep complete");
                         signAndSubmitCancel(r.getResult().getMultisig_txset(), table);
                    });
                });
            });
        });
    }

    /**
     * Helper method for mediator xmr wallet recovery of funds.
     * @param txset
     * @param quote
     */
    private void signAndSubmitCancel(String txset, XmrQuoteTable quote) {
        String sfn = quote.getSwap_filename();
         monero.controlWallet(WalletState.OPEN, sfn).subscribe(o -> {
             monero.signMultisig(txset).subscribe(r -> {
                // null check, since rpc since 200 on null result
                if(r.getResult() == null) {
                    logger.error(Constants.MULTISIG_CONFIG_ERROR);
                }
                 monero.submitMultisig(txset).subscribe(s -> {
                    logger.info("Cancel tx: {}", s.getResult().getTx_hash_list().get(0));
                    monero.controlWallet(WalletState.CLOSE, sfn).subscribe(c -> {
                        SwapService.isWalletOpen = false;
                        logger.info("Cancel complete");
                    });
                });
            });
        });
    }
    
}
