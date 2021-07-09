package com.hiahatf.mass.repo;

import org.springframework.stereotype.Repository;

import com.hiahatf.mass.models.monero.XmrQuoteTable;

import org.springframework.data.repository.CrudRepository;  

/**
 * Interface for performing database operations on the quote table
 */
@Repository
public interface QuoteRepository extends CrudRepository<XmrQuoteTable, String> 
{
    
}
