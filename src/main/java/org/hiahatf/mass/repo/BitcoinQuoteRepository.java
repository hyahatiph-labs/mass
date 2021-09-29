package org.hiahatf.mass.repo;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.BitcoinQuote;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface for performing database operations on the Bitcoin quote table
 */
@Repository(Constants.BTC_QUOTE_REPO)
public interface BitcoinQuoteRepository extends CrudRepository<BitcoinQuote, String>
{
    
}