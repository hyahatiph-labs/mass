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
    private String quoteId;
    private String address;
    private Boolean isValidAddress;
    private double amount;
    private Double rate;
    private String invoice;
}
