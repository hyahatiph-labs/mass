package com.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroRequest {
    // valid monero address
    private String address;
    // amount in monero
    private double amount;
}
