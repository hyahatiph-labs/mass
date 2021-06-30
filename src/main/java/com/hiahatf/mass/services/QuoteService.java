package com.hiahatf.mass.services;

import com.hiahatf.mass.models.MoneroQuote;

import org.springframework.stereotype.Service;

@Service("QuoteService")
public class QuoteService {
    
    // TODO: get rate data

    // TODO: validate address from request

    // TODO: save quote to db with status

    // TODO: generate lightning invoice for the quote

    // TODO: return quote with necessary data

    /**
     * Helper method for building the monero quote
     * and returning it to the client
     * @return MoneroQuote
     */
    public MoneroQuote buildMoneroQuote() {
        return MoneroQuote.builder()
        .address("4skljafjl")
        .amount(1.0)
        .invoice("lninvoice123")
        .rate(0.006)
        .build();
    }
}
