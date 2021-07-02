package com.hiahatf.mass.models;

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
    private String address;
    private double amount;
    private Double rate;
    private String invoice;
}
