package org.hiahatf.mass.repo;

import org.springframework.stereotype.Repository;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.MoneroQuote;

import org.springframework.data.repository.CrudRepository;  

/**
 * Interface for performing database operations on the Monero quote table
 */
@Repository(Constants.XMR_QUOTE_REPO)
public interface MoneroQuoteRepository extends CrudRepository<MoneroQuote, String>
{
    
}
