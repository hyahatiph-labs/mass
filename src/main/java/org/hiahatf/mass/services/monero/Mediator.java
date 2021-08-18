package org.hiahatf.mass.services.monero;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.rpc.Lightning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * Runnable triggered by the ScheduledExecutor for mediating
 * the consensus wallet for unresolvable swaps in order to
 * prevent mlof (mass loss of funds).
 */
public class Mediator implements Runnable {

    private Logger logger = LoggerFactory.getLogger(Mediator.class);
    private MoneroQuoteRepository quoteRepository;
    private Lightning lightning;
    private String quoteId;
    
    /**
    * After the 30 minute consensus wallet time for swap finality
    * revert the funds back to the mass wallet. This swap is no longer
    * valid and is removed from the database
    * @param quoteId
    */
    public Mediator(MoneroQuoteRepository quoteRepository, String quoteId, Lightning lightning) {
        this.quoteRepository = quoteRepository;
        this.lightning = lightning;
        this.quoteId = quoteId;
    }

    public void run() {
        try {
            XmrQuoteTable table = quoteRepository.findById(quoteId).get();
            lightning.handleInvoice(table, true).subscribe(r -> {
                if(r.getStatusCode() != HttpStatus.OK) {
                    logger.error("Mediator failed to settle");
                }
                logger.info("Mediator execution complete");
            });
        } catch (SSLException se) {
            logger.error(Constants.UNK_ERROR);
        } catch (IOException ie) {
            logger.error(Constants.UNK_ERROR);
        } 
    }
    
}
