package com.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

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
    private String rate;
    private String invoice;
}
