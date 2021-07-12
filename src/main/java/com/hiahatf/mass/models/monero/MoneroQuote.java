package com.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero quote
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroQuote {
    // quote id is the same as lnd payment hash  
    private String quoteId;
    // monero rx address
    private String address;
    // proof of address validity
    private Boolean isValidAddress;
    // amount in monero
    private double amount;
    // this the mass rate including markup?
    private Double rate;
    // lightning network invoice to pay
    private String invoice;
}
