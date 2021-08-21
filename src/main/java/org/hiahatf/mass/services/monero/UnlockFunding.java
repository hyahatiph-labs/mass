package org.hiahatf.mass.services.monero;

import org.hiahatf.mass.models.FundingState;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.repo.MoneroQuoteRepository;

// TODO: convert funding unlock executor to scheduler

/**
 * Runnable triggered by the ScheduledExecutor for unlocking
 * the consensus wallet funding transactions
 */
public class UnlockFunding implements Runnable {

    private MoneroQuoteRepository quoteRepository;
    private String quoteId;
    
    /**
    * After the 20 minute consensus wallet time lock update the swap
    * funding state to COMPLETE. This is called by the ScheduledExecutor.
    * Finalization of the swap is verified against the updated state.
    * @param quoteId
    */
    public UnlockFunding(String quoteId, MoneroQuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
        this.quoteId = quoteId;
    }

    public void run() {
        XmrQuoteTable table = quoteRepository.findById(quoteId).get();
        table.setFunding_state(FundingState.COMPLETE);
        quoteRepository.save(table);
    }
    
}
